import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import HomePage from './pages/HomePage'; // 이 부분이 새 홈페이지를 불러오도록 합니다.
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler';
import ProfilePage from './pages/ProfilePage';
import OAuth2RegisterPage from './pages/OAuth2RegisterPage';
import EventDetailPage from './pages/EventDetailPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import ResendVerificationPage from './pages/ResendVerificationPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';
import TossSuccessPage from './pages/TossSuccessPage';
import TossFailPage from './pages/TossFailPage';
import QueueWaitingPage from './pages/QueueWaitingPage';
import VenueAdminPage from './pages/VenueAdminPage';
import EventAdminPage from './pages/EventAdminPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import ShowtimeAdminPage from './pages/ShowtimeAdminPage';
import SeatAdminPage from './pages/SeatAdminPage';
import EventWizardPage from './pages/EventWizardPage';
import SeatMapperPage from './pages/SeatMapperPage';

/**
 * 애플리케이션의 최상위 컴포넌트입니다.
 * 전체 레이아웃과 페이지 라우팅을 설정합니다.
 * @returns {JSX.Element} App 컴포넌트
 */
function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/auth/oauth2/redirect" element={<OAuth2RedirectHandler />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/auth/oauth2/register" element={<OAuth2RegisterPage />} />
        <Route path="/events/:eventId" element={<EventDetailPage />} />
        <Route path="/book/event/:eventId/showtime/:showtimeId/queue" element={<QueueWaitingPage />} />
        <Route path="/book/showtime/:showtimeId/seats" element={<SeatSelectionPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:orderId" element={<OrderDetailPage />} />
        <Route path="/payments/toss/success" element={<TossSuccessPage />} />
        <Route path="/payments/toss/fail" element={<TossFailPage />} />
        <Route path="/admin/venues" element={<VenueAdminPage />} />
        <Route path="/admin/events" element={<EventAdminPage />} />
        <Route path="/admin/showtimes" element={<ShowtimeAdminPage />} />
        <Route path="/admin/seats" element={<SeatAdminPage />} />
        <Route path="/admin/wizard" element={<EventWizardPage />} />
        <Route path="/admin/seat-mapper" element={<SeatMapperPage />} />
        <Route path="/verify-email" element={<EmailVerificationPage />} />
        <Route path="/resend-verification" element={<ResendVerificationPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Routes>
    </Layout>
  );
}

export default App;
