import Api from './api';

function getSites() {
    return Api.get('/manufacturers/sites');
};

function getSiteDetails(id) {
    return Api.get(`/manufacturers/sites/${id}`);
};

function createNewSite(body) {
    return Api.post(`/manufacturers/sites`);
};

function createNewSite(id, body) {
    return Api.put(`/manufacturers/sites/${id}`, body);
};

function createNewSite(id) {
    return Api.delete(`/manufacturers/sites/${id}`);
};

export default {
    getSites,
    getSiteDetails,
    createNewSite
};