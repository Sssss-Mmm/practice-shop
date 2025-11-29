import React from 'react';
import { AppBar, Toolbar, Typography, Container, Box } from '@mui/material';
import { Link } from 'react-router-dom';

const Layout = ({ children }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
              Ticketing Shop
            </Link>
          </Typography>
          <Link to="/login" style={{ textDecoration: 'none', color: 'inherit' }}>
            <Typography>Login</Typography>
          </Link>
        </Toolbar>
      </AppBar>
      <Container component="main" sx={{ flexGrow: 1, mt: 4, mb: 4 }}>
        {children}
      </Container>
      <Box
        component="footer"
        sx={{
          py: 3,
          px: 2,
          mt: 'auto',
          backgroundColor: (theme) =>
            theme.palette.mode === 'light'
              ? theme.palette.grey[200]
              : theme.palette.grey[800],
        }}
      >
        <Container maxWidth="sm">
          <Typography variant="body1" align="center">
            My Ticketing Shop
          </Typography>
          <Typography variant="body2" color="text.secondary" align="center">
            {'Copyright © '}
            <Link to="/" style={{color: 'inherit'}}>
              Your Website
            </Link>{' '}
            {new Date().getFullYear()}
            {'.'}
          </Typography>
        </Container>
      </Box>
    </Box>
  );
};

export default Layout;
