import React, { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import EventService from '../services/event.service';

// MUI Imports
import { 
    Container, 
    Typography, 
    Grid, 
    Card, 
    CardMedia, 
    CardContent, 
    CardActionArea,
    Box,
    Paper
} from '@mui/material';

const HomePage = () => {
    const [eventsByCategory, setEventsByCategory] = useState({});

    useEffect(() => {
        // Fetch events that are currently on sale
        EventService.listEvents('SALE').then(
            (response) => {
                const events = response.data;
                // Group events by category
                const groupedEvents = events.reduce((acc, event) => {
                    const category = event.category || '기타';
                    if (!acc[category]) {
                        acc[category] = [];
                    }
                    acc[category].push(event);
                    return acc;
                }, {});
                setEventsByCategory(groupedEvents);
            },
            (error) => {
                console.error("Error fetching events", error);
            }
        );
    }, []);

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) {
            return '/placeholder.png'; // Make sure you have a placeholder image in public folder
        }
        if (relativeUrl.startsWith('http')) {
            return relativeUrl;
        }
        const apiBase = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084';
        return `${apiBase}${encodeURI(relativeUrl)}`;
    };

    return (
        <Container maxWidth="lg">
            {/* Promotional Banner */}
            <Paper 
                elevation={4} 
                sx={{ 
                    p: 4, 
                    mb: 5, 
                    mt: 2,
                    textAlign: 'center', 
                    backgroundColor: 'primary.main', 
                    color: 'white' 
                }}
            >
                <Typography variant="h3" component="h1" gutterBottom>
                    놓치고 싶지 않은 순간, 지금 바로 예매하세요
                </Typography>
                <Typography variant="h6" component="p">
                    콘서트, 뮤지컬, 스포츠 등 다양한 즐거움을 한눈에.
                </Typography>
            </Paper>

            {/* Event Listings */}
            {Object.keys(eventsByCategory).map(category => (
                <Box key={category} sx={{ mb: 5 }}>
                    <Typography variant="h4" component="h2" gutterBottom sx={{ borderBottom: 1, borderColor: 'divider', pb: 1, mb: 3 }}>
                        {category}
                    </Typography>
                    <Grid container spacing={4}>
                        {eventsByCategory[category].map((event) => (
                            <Grid item key={event.id} xs={12} sm={6} md={4} lg={3}>
                                <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                                    <CardActionArea component={RouterLink} to={`/events/${event.id}`}>
                                        <CardMedia
                                            component="img"
                                            height="300"
                                            image={resolveImageUrl(event.posterImageUrl)}
                                            alt={event.title}
                                            sx={{ objectFit: 'cover' }}
                                        />
                                        <CardContent sx={{ flexGrow: 1 }}>
                                            <Typography gutterBottom variant="h6" component="h3" noWrap>
                                                {event.title}
                                            </Typography>
                                            <Typography variant="body2" color="text.secondary">
                                                {event.venue.name}
                                            </Typography>
                                        </CardContent>
                                    </CardActionArea>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            ))}
        </Container>
    );
};

export default HomePage;
