import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler';
import ProfilePage from './pages/ProfilePage';
import OAuth2RegisterPage from './pages/OAuth2RegisterPage';
import ProductRegistrationPage from './pages/ProductRegistrationPage';
import ProductDetailPage from './pages/ProductDetailPage';
import EmailVerificationPage from './pages/EmailVerificationPage';
import ResendVerificationPage from './pages/ResendVerificationPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import CartPage from './pages/CartPage';
import ProductEditPage from './pages/ProductEditPage';

function App() {
  return (
    <div>
      <Header />
      <main className="container mt-3">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/auth/oauth2/redirect" element={<OAuth2RedirectHandler />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/auth/oauth2/register" element={<OAuth2RegisterPage />} />
          <Route path="/product-registration" element={<ProductRegistrationPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/verify-email" element={<EmailVerificationPage />} />
          <Route path="/resend-verification" element={<ResendVerificationPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="products/:productId/edit" element={<ProductEditPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default App;
