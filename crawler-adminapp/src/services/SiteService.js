import { api } from './api';

/**
 * Returns list of websites
 */
function fetchSites() {
    return api().get('/websites');
}

/**
 * Fetches the website identified by the id
 * @param string id - ID of the website to get
 */
function fetchSiteDetails(id) {
    return api().get(`/websites/${id}`);
}

/**
 * Creates a new website
 * @param object body - The site details
 */
function createNewSite(body) {
    return api().post(`/websites`, body);
}

/**
 * Updates a website's details
 * @param object body - The site details
 */
function updateSite(body) {
    let id = body.id;
    delete body.id;
    return api().put(`/websites/${id}`, body);
}

/**
 * Deletes a website
 * @param object id - The ID of the website to delete
 */
function deleteSite(id) {
    return api().delete(`/websites/${id}`);
}

export default {
    fetchSites,
    fetchSiteDetails,
    createNewSite,
    updateSite,
    deleteSite
};