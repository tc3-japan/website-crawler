import axios from 'axios';
import Store from '../store';

function createAxiosInstance(contentType) {
    return axios.create({
        baseURL: process.env.VUE_APP_API_BASE_URL,
        withCredentials: true,
        headers: {
            'Accept': 'application/json',
            'Content-Type': contentType ? contentType : 'application/json',
            'Access-Control-Allow-Origin': '*'
        }
    });
}

let apiInstance = createAxiosInstance();

apiInstance.interceptors
.request
.use(function (config) {
    if (Store.state.token)
        config.headers['Authorization'] = `Bearer ${Store.state.token}`;
    else {
        delete config.headers['Authorization'];
    }
    return config;
});

let authInstance = createAxiosInstance('application/x-www-form-urlencoded');
/**
 * Returns axios instance for accessing api
 */
export function api() {
    return apiInstance;
}

/**
 * Returns axios instance for accessing api
 */
export function auth() {
    return authInstance;
}