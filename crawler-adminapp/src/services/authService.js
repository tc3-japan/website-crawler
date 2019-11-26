import { auth } from './api';
import Store from '@/store';
import qs from'querystring';

/**
 * Calls API login endpoint
 * @param string username 
 * @param string password 
 */
function logIn(username, password) {
    return new Promise((resolve, reject) => {
        auth().post('/oauth/token', qs.stringify({ username, password }))
        .then(response => {
            Store.commit('setToken', response.data.access_token);
            resolve(response);
        })
        .catch(err => {
            reject(err);
        });
    });
}

/**
 * Calls API login endpoint
 */
function logOut() {
    Store.commit('setToken', null);
    return Promise.resolve(null); // auth().get('/oauth/logout')
}

export default {
    logIn,
    logOut
};