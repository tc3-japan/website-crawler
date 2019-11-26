import { auth } from './api';
import Store from '@/store';

/**
 * Calls API login endpoint
 * @param string username 
 * @param string password 
 */
function logIn(username, password) {
    return new Promise((resolve, reject) => {
        auth().post('/token', { username, password })
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
    return auth().get('/logout');
}

export default {
    logIn,
    logOut
};