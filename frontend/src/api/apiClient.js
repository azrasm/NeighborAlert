import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE;

// Kreiranje Axios instance (zamjena za stari api.request)
const httpClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: lijepi JWT token na svaki zahtjev (P-08)
httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('na_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Ako server vrati 401, brise token i izbacuje korisnika (P-08)
httpClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('na_token');
      window.location.href = '/login'; // Preusmjeravanje na login
    }

    const errMsg = error.response?.data?.message || `HTTP ${error.response?.status || 'Network Error'}`;
    return Promise.reject(new Error(errMsg));
  }
);

//  API metode (P-07)
export const login = (body) => httpClient.post('/api/auth/login', body);
export const getUsers = () => httpClient.get('/api/users');
export const getUser = (id) => httpClient.get(`/api/users/${id}`);
export const getReports = () => httpClient.get('/api/reports');
export const getReport = (id) => httpClient.get(`/api/reports/${id}`);
export const createReport = (body) => httpClient.post('/api/reports', body);
export const deleteReport = (id) => httpClient.delete(`/api/reports/${id}`);
export const getCommentsByReport = (reportId) => httpClient.get(`/api/comments/report/${reportId}`);
export const addComment = (body) => httpClient.post('/api/comments', body);
export const getAssignments = () => httpClient.get('/api/administration/assignments');
export const assignReport = (body) => httpClient.post('/api/administration/assign', body);
export const updateStatus = (body) => httpClient.post('/api/administration/status/change', body);
export const updateStatusNormal = (body) => httpClient.post('/api/administration/status', body);

// Notifications and Media
export const getNotifications = (id) => httpClient.get(`/api/notifications/user/${id}`);
export const markAsRead = (id) => httpClient.post(`/api/notifications/${id}/read`);
export const addMedia = (body) => httpClient.post('/api/media', body);
export const createFlag = (body) => httpClient.post('/api/flags', body);
export const getFlags = () => httpClient.get('/api/flags');
export const getUnreviewedFlags = () => httpClient.get('/api/flags/unreviewed');
export const reviewFlag = (id, reviewed) => httpClient.patch(`/api/flags/${id}/review`, { reviewed });

const api = {
  login, getUsers, getUser, getReports, getReport, createReport, deleteReport,
  getCommentsByReport, addComment, getAssignments, assignReport, updateStatus,
  updateStatusNormal, getNotifications, markAsRead, addMedia, createFlag, getFlags,
  getUnreviewedFlags, reviewFlag
};
export default api;