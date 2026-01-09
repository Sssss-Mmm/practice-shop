import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import EventService from '../services/event.service';
import './HomePage.css';
import { FaFire, FaTicketAlt, FaChevronRight } from 'react-icons/fa';

/**
 * 메인 홈페이지 컴포넌트 (UI/UX 2.0)
 * 인터파크/티켓링크 스타일의 포털형 구조
 */
const HomePage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentBanner, setCurrentBanner] = useState(0);

    // Mock Banners
    const banners = [
        { id: 1, title: "오페라의 유령 월드투어", desc: "전 세계를 울린 감동, 드디어 한국 상륙!", image: "https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&q=80&w=2070" },
        { id: 2, title: "2024 야구 개막전", desc: "심장을 울리는 함성, 다시 시작된다!", image: "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&q=80&w=2070" }
    ];

    const [searchParams] = useSearchParams();
    const keyword = searchParams.get('q');
    const category = searchParams.get('category');

    useEffect(() => {
        setLoading(true);
        const params = {};
        if (keyword) params.keyword = keyword;
        if (category) params.category = category;

        EventService.listEvents(params)
            .then((response) => {
                const data = Array.isArray(response.data) ? response.data : response.data?.content;
                setProducts(data || []);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [keyword, category]);

    // Auto rotate banners
    useEffect(() => {
        const interval = setInterval(() => {
            setCurrentBanner(prev => (prev + 1) % banners.length);
        }, 5000);
        return () => clearInterval(interval);
    }, [banners.length]);

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) return '/placeholder.png';
        if (relativeUrl.startsWith('http')) return relativeUrl;
        const apiBase = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8084';
        return `${apiBase}${relativeUrl}`;
    };

    // Derived Data
    const rankingEvents = products.slice(0, 5); // Just take first 5 for now
    const ticketOpenEvents = products.slice().reverse().slice(0, 5); // Newest first
    const hotPicks = products.filter(p => !p.category || p.category === 'STAGE').slice(0, 6);

    return (
        <div className="homepage-container">
            {/* 1. Hero Banner */}
            <section className="hero-banner-section">
                <div 
                    className="hero-banner-content"
                    style={{ backgroundImage: `url(${banners[currentBanner].image})` }}
                >
                    <div className="hero-overlay">
                        <h1>{banners[currentBanner].title}</h1>
                        <p>{banners[currentBanner].desc}</p>
                    </div>
                </div>
                <div className="banner-controls">
                    {banners.map((_, idx) => (
                        <div 
                            key={idx} 
                            className={`banner-dot ${currentBanner === idx ? 'active' : ''}`}
                            onClick={() => setCurrentBanner(idx)}
                        />
                    ))}
                </div>
            </section>

            <div className="main-inner">
                {/* 2. Ranking Section */}
                <section className="ranking-section">
                    <div className="section-header">
                        <h2 className="section-title">랭킹 <FaFire style={{color: '#FF3434', fontSize: '20px'}}/></h2>
                        <Link to="/ranking" className="view-all-link">전체보기 <FaChevronRight /></Link>
                    </div>
                    <div className="ranking-container">
                        {rankingEvents.map((event, idx) => (
                            <Link to={`/events/${event.eventId || event.id}`} key={event.eventId || event.id} className="ranking-item">
                                <div className="ranking-badge">{idx + 1}</div>
                                <div className="ranking-thumb">
                                    <img src={resolveImageUrl(event.posterImageUrl || event.imageUrls?.[0])} alt={event.title} />
                                </div>
                                <div className="ranking-info">
                                    <div className="ranking-title">{event.title}</div>
                                    <div className="ranking-date">
                                        {event.salesStartDate} ~ {event.salesEndDate}
                                    </div>
                                </div>
                            </Link>
                        ))}
                    </div>
                </section>

                {/* 3. Grid Content (Ticket Open + Picks) */}
                <div className="content-grid">
                    {/* Left: Ticket Open */}
                    <aside className="ticket-open-wrapper">
                        <div className="ticket-open-list">
                            <h3>티켓 오픈 <FaTicketAlt style={{color: '#E53E3E'}}/></h3>
                            {ticketOpenEvents.map(event => (
                                <Link to={`/events/${event.eventId || event.id}`} key={event.eventId || event.id} className="open-item" style={{textDecoration: 'none', color: 'inherit'}}>
                                    <img src={resolveImageUrl(event.posterImageUrl)} className="open-thumb" alt="" />
                                    <div className="open-info">
                                        <div className="open-title">{event.title}</div>
                                        <div className="open-date">{event.salesStartDate || '오픈 예정'}</div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    </aside>

                    {/* Right: Weekend Picks */}
                    <section className="picks-wrapper">
                        <div className="section-header">
                            <h2 className="section-title">주말 핫 픽! 공연</h2>
                            <Link to="/events" className="view-all-link">더보기 <FaChevronRight /></Link>
                        </div>
                        <div className="picks-grid">
                            {hotPicks.map(event => (
                                <Link to={`/events/${event.eventId || event.id}`} key={event.eventId} className="pick-card">
                                    <div className="pick-thumb-wrapper">
                                        <img src={resolveImageUrl(event.posterImageUrl)} className="pick-thumb" alt={event.title} />
                                    </div>
                                    <div className="pick-title">{event.title}</div>
                                    <div className="pick-venue">{event.venueName}</div>
                                </Link>
                            ))}
                        </div>
                    </section>
                </div>
            </div>
        </div>
    );
};

export default HomePage;
