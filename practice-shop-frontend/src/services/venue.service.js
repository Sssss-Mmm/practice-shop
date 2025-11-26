import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/venues';
const authConfig = () => ({ headers: AuthService.authHeader() });

const listVenues = () => axios.get(API_URL, authConfig());
const getVenue = (venueId) => axios.get(`${API_URL}/${venueId}`, authConfig());
const createVenue = (payload) => axios.post(API_URL, payload, authConfig());

const VenueService = {
    listVenues,
    getVenue,
    createVenue,
};

export default VenueService;
