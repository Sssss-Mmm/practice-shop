import React from 'react';
import { Container, Box } from '@mui/material';
import { Link } from 'react-router-dom';
import Header from './Header';
import './Layout.css';

const Layout = ({ children }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <Container component="main" sx={{ flexGrow: 1, mt: 4, mb: 4 }}>
        {children}
      </Container>
      <Box component="footer" className="app-footer">
        <Container maxWidth="sm">
          <p className="footer-title">My Ticketing Shop</p>
          <p className="footer-copy">
            {'Copyright © '}
            <Link to="/" className="footer-link">
              Your Website
            </Link>{' '}
            {new Date().getFullYear()}
            {'.'}
          </p>
        </Container>
      </Box>
    </Box>
  );
};

export default Layout;
