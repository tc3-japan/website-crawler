import axios from 'axios';
import config from '../../config';
import Store from '../store';

/**
 * Adds access token to axios headers and adds to store. 
 * If token passed is null, header/store value is removed.
 * @param string token - The access token to set
 */
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

/**
 * Returns axios instance for accessing api
 */
export function api() {
    // TODO: remove temporary authentication details
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

// On start check if there is a token in store, and set headers
if (Store.state.token) {
    setToken(Store.state.token);
}