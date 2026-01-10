import React, { useState, useEffect } from 'react';
import { FaStar, FaTrash } from 'react-icons/fa';
import ReviewService from '../services/review.service';
import { useAuth } from '../context/AuthContext';
import './ReviewSection.css';

const ReviewSection = ({ eventId }) => {
    const { currentUser } = useAuth();
    const [reviews, setReviews] = useState([]);
    const [rating, setRating] = useState(5);
    const [content, setContent] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadReviews();
    }, [eventId]);

    const loadReviews = () => {
        ReviewService.listReviews(eventId)
            .then(res => setReviews(res.data))
            .catch(err => console.error(err));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!content.trim()) return;

        setLoading(true);
        try {
            await ReviewService.createReview(eventId, { rating, content });
            setContent('');
            setRating(5);
            loadReviews();
        } catch (error) {
            alert('리뷰 작성 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (reviewId) => {
        if (!window.confirm('리뷰를 삭제하시겠습니까?')) return;
        try {
            await ReviewService.deleteReview(reviewId);
            loadReviews();
        } catch (error) {
            alert('리뷰 삭제 실패');
        }
    };

    return (
        <div className="review-section">
            <h3 className="section-title">관람 후기 ({reviews.length})</h3>

            {currentUser ? (
                <form className="review-form" onSubmit={handleSubmit}>
                    <div className="rating-select">
                        {[1, 2, 3, 4, 5].map(star => (
                            <FaStar
                                key={star}
                                className={`star-icon ${star <= rating ? 'active' : ''}`}
                                onClick={() => setRating(star)}
                            />
                        ))}
                    </div>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="공연에 대한 후기를 남겨주세요."
                        required
                    />
                    <button type="submit" disabled={loading}>
                        {loading ? '등록 중...' : '등록'}
                    </button>
                </form>
            ) : (
                <div className="login-prompt">
                    후기를 작성하려면 로그인이 필요합니다.
                </div>
            )}

            <div className="review-list">
                {reviews.map(review => (
                    <div key={review.reviewId} className="review-item">
                        <div className="review-header">
                            <div className="reviewer-name">{review.userName || '익명'}</div>
                            <div className="review-rating">
                                {[...Array(5)].map((_, i) => (
                                    <FaStar key={i} className={i < review.rating ? 'active' : 'inactive'} />
                                ))}
                            </div>
                            <div className="review-date">
                                {new Date(review.createdAt).toLocaleDateString()}
                            </div>
                            {review.isOwner && (
                                <button className="delete-btn" onClick={() => handleDelete(review.reviewId)}>
                                    <FaTrash /> 삭제
                                </button>
                            )}
                        </div>
                        <div className="review-content">{review.content}</div>
                    </div>
                ))}
                {reviews.length === 0 && <div className="no-reviews">첫 번째 후기를 남겨주세요!</div>}
            </div>
        </div>
    );
};

export default ReviewSection;
