import axios from 'axios';
import config from '../../config';
import Store from '../store';

export function setToken(token) {
    if (token == null) {
        delete axios.defaults.headers.common['Authorization'];
        Store.commit('setToken', null);
    }
    else {
        axios.defaults.headers.common['Authorization'] = token;
        Store.commit('setToken', token);
    }
}

export function api() {
    // TODO: check whic 
    return axios.create({
        baseURL: config.api.baseURL,
        withCredentials: true,
        auth: {
            username: 'admin',
            password: 'adminpass'
        },
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        }
    });
}

if (Store.state.token) {
    setToken(Store.state.token);
}