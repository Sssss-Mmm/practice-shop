#!/usr/bin/env python3
"""대기열 부하 테스트.

다수의 가상 유저(쓰레드)가 동시에 대기열에 진입(enter)한 뒤,
입장 가능(ready) 상태가 될 때까지 순번(status)을 폴링하는 시나리오를 시뮬레이션한다.

서버를 부하 테스트 모드로 띄워야 한다. 기본 모드에서는 대기열 진입에 로그인이 필요하다(401).
  java -jar build/libs/practice-shop-*.jar --queue.load-test-mode=true

엔드포인트 (부하 테스트 모드에서만 userId 파라미터로 진입 허용):
  POST /api/queue/enter/{eventId}?userId=...  -> {token, position}
  GET  /api/queue/status?token=...            -> {ready, position}

사용:
  python3 scripts/load_test_queue.py --users 200 --event 1
  python3 scripts/load_test_queue.py --url http://localhost:8084 --users 500
"""
import argparse
import json
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed

# ponytail: stdlib urllib, requests/locust 안 씀. 더 정밀한 지표/램프업 필요하면 locust로 교체.


def _post(url, timeout):
    req = urllib.request.Request(url, data=b"", method="POST")
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return json.load(r)


def _get(url, timeout):
    with urllib.request.urlopen(url, timeout=timeout) as r:
        return json.load(r)


def virtual_user(base, event_id, user_id, polls, poll_interval, timeout):
    """한 유저의 진입+폴링. (성공여부, 진입소요초, 첫 position) 반환."""
    start = time.perf_counter()
    enter_url = f"{base}/api/queue/enter/{event_id}?userId={urllib.parse.quote(user_id)}"
    try:
        enter = _post(enter_url, timeout)
        enter_latency = time.perf_counter() - start  # 폴링 sleep이 섞이지 않게 진입 API만 측정
    except (urllib.error.URLError, OSError, ValueError) as e:
        return False, time.perf_counter() - start, None, repr(e)

    token = enter.get("token")
    first_pos = enter.get("position")
    status_url = f"{base}/api/queue/status?token={urllib.parse.quote(token)}"
    for _ in range(polls):
        try:
            st = _get(status_url, timeout)
        except (urllib.error.URLError, OSError, ValueError) as e:
            return False, enter_latency, first_pos, repr(e)
        if st.get("ready"):
            break
        time.sleep(poll_interval)
    return True, enter_latency, first_pos, None


def main():
    p = argparse.ArgumentParser(description="Redis 대기열 부하 테스트")
    p.add_argument("--url", default="http://localhost:8084", help="서버 베이스 URL")
    p.add_argument("--event", type=int, default=1, help="대상 eventId")
    p.add_argument("--users", type=int, default=200, help="동시 가상 유저 수")
    p.add_argument("--workers", type=int, default=50, help="동시 실행 쓰레드 수")
    p.add_argument("--polls", type=int, default=10, help="유저당 최대 status 폴링 횟수")
    p.add_argument("--poll-interval", type=float, default=1.0, help="폴링 간격(초)")
    p.add_argument("--timeout", type=float, default=10.0, help="HTTP 타임아웃(초)")
    args = p.parse_args()

    base = args.url.rstrip("/")
    print(f"[load] {args.users} users -> {base} (event={args.event}, workers={args.workers})")

    wall_start = time.perf_counter()
    results = []
    with ThreadPoolExecutor(max_workers=args.workers) as pool:
        futures = [
            pool.submit(virtual_user, base, args.event, f"loadtest-{i}",
                        args.polls, args.poll_interval, args.timeout)
            for i in range(args.users)
        ]
        for f in as_completed(futures):
            results.append(f.result())
    wall = time.perf_counter() - wall_start

    ok = [r for r in results if r[0]]
    failed = [r for r in results if not r[0]]
    latencies = sorted(r[1] for r in ok)

    print(f"\n[result] total={len(results)} ok={len(ok)} failed={len(failed)} wall={wall:.2f}s")
    if latencies:
        n = len(latencies)
        print(f"  latency  avg={sum(latencies)/n:.3f}s  min={latencies[0]:.3f}s  "
              f"p50={latencies[n//2]:.3f}s  p95={latencies[min(n-1, int(n*0.95))]:.3f}s  "
              f"max={latencies[-1]:.3f}s")
    if failed:
        sample = failed[0][3]
        print(f"  first error: {sample}")


if __name__ == "__main__":
    main()
