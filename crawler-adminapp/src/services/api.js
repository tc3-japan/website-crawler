import axios from 'axios';
import VueAxios from 'vue-axios';
import Vue from 'vue';
import Store from '../store';

Vue.use(VueAxios, axios);

function createAxiosInstance(baseURL) {
    return axios.create({
        baseURL: baseURL,
        withCredentials: true,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        }
    });
}

let apiInstance = createAxiosInstance(process.env.VUE_APP_API_BASE_URL.replace(/\/?$/, '/') + 'api');

apiInstance.interceptors
.request
.use(function (config) {
    console.log('setting token', Store.state.token);
    if (Store.state.token)
        config.headers['Authorization'] = `Bearer ${Store.state.token}`;
    else {
        delete config.headers['Authorization'];
    }
    return config;
});

let authInstance = createAxiosInstance(process.env.VUE_APP_API_BASE_URL.replace(/\/?$/, '/') + 'oauth');
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