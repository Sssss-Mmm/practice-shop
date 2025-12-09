import React from 'react';


const Footer = () => {
    return (
        <footer style={{ backgroundColor: '#eee', padding: '40px 0', borderTop: '1px solid #ddd', marginTop: 'auto', color: '#666', fontSize: '13px' }}>
            <div className="container">
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                    <div style={{ display: 'flex', gap: '20px', fontWeight: 'bold', color: '#333' }}>
                        <span>회사소개</span>
                        <span>이용약관</span>
                        <span>개인정보처리방침</span>
                        <span>청소년보호정책</span>
                    </div>
                    <div>
                        <span style={{ marginRight: '10px' }}>고객센터 1544-1555</span>
                        <span>1:1문의 바로가기</span>
                    </div>
                </div>
                <div style={{ lineHeight: '1.6' }}>
                    <strong>(주)티켓존</strong> <br />
                    대표이사 : 홍길동 | 사업자등록번호 : 123-45-67890 | 통신판매업신고 : 2024-서울강남-00000 <br />
                    주소 : 서울특별시 강남구 테헤란로 123, 4층 (역삼동) | 이메일 : help@ticketzone.com <br />
                    Copyright © TICKETZONE Corp. All Rights Reserved.
                </div>
            </div>
        </footer>
    );
};

export default Footer;
