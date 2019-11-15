import { api, setToken } from './api';

function logIn(username, password) {
    return new Promise((resolve, reject) => {
        api().post('/oauth/token', { username, password })
        .then(response => {
            setToken(response.data.token);
            resolve(response);
        })
        .catch(err => {
            reject(err);
        });
    });
}

function logOut() {
    setToken(null);
    return api().get('/oauth/logout');
}

export default {
    logIn,
    logOut
};