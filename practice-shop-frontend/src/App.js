import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler';
import ProfilePage from './pages/ProfilePage';
import OAuth2RegisterPage from './pages/OAuth2RegisterPage';
import ProductRegistrationPage from './pages/ProductRegistrationPage';
import ProductDetailPage from './pages/ProductDetailPage';
import EventDetailPage from './pages/EventDetailPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import ResendVerificationPage from './pages/ResendVerificationPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';
import ProductEditPage from './pages/ProductEditPage';
import TossSuccessPage from './pages/TossSuccessPage';
import TossFailPage from './pages/TossFailPage';
import VenueAdminPage from './pages/VenueAdminPage';
import EventAdminPage from './pages/EventAdminPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import ShowtimeAdminPage from './pages/ShowtimeAdminPage';
import SeatAdminPage from './pages/SeatAdminPage';

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
        <Route path="/product-registration" element={<ProductRegistrationPage />} />
        <Route path="/products/:productId" element={<ProductDetailPage />} />
        <Route path="/events/:eventId" element={<EventDetailPage />} />
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
        <Route path="/verify-email" element={<EmailVerificationPage />} />
        <Route path="/resend-verification" element={<ResendVerificationPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="products/:productId/edit" element={<ProductEditPage />} />
      </Routes>
    </Layout>
  );
}

export default App;
