<template>
  <b-modal id="site-details" :title="title" size="xl" scrollable @show="show" @close="close">
    <div class="card-body">
      <form class>
        <div class="position-relative row form-group">
          <label for="name" class="col-sm-2 col-form-label">Name</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="name"
              id="name"
              v-model="site.name"
              placeholder="Enter the site name"
              type="text"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="description" class="col-sm-2 col-form-label">Description</label>
          <div class="col-sm-10">
            <textarea :readonly="readOnly" name="description" id="description" v-model="site.description" class="form-control"></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="url" class="col-sm-2 col-form-label">URL</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="url"
              id="url"
              v-model="site.url"
              placeholder="Enter the site URL"
              type="text"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="contentUrlPatterns" class="col-sm-2 col-form-label">Product Page URL Pattern</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="contentUrlPatterns"
              id="contentUrlPatterns"
              v-model="site.contentUrlPatterns"
              placeholder="Enter the site product page URL pattern"
              type="text"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="categoryExtractionPattern" class="col-sm-2 col-form-label">Category Extraction Pattern</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="categoryExtractionPattern"
              id="categoryExtractionPattern"
              v-model="site.categoryExtractionPattern"
              placeholder="Enter the site category extraction pattern"
              type="text"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="contentSelectors" class="col-sm-2 col-form-label">Selectors</label>
          <div class="col-sm-10">
            <textarea :readonly="readOnly" name="contentSelectors" id="contentSelectors" v-model="site.contentSelectors" class="form-control"></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="supportsRobotsTxt" class="col-sm-2 col-form-label">Robots.txt</label>
          <div class="col-sm-10">
            <toggle-button 
              :disabled="readOnly"
              name="supportsRobotsTxt"
              id="supportsRobotsTxt"
              :width="65"
              :height="35"
              v-model="site.supportsRobotsTxt"
              :labels="true"
              :color="{checked: '#28a745', unchecked: '#434055'}"
              :font-size="14"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawlMaxDepth" class="col-sm-2 col-form-label">Depth of Crawl</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawlMaxDepth"
              id="crawlMaxDepth"
              v-model="site.crawlMaxDepth"
              placeholder="Depth of crawl"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawlTimeLimit" class="col-sm-2 col-form-label">Max Time of Crawl (Seconds)</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawlTimeLimit"
              id="crawlTimeLimit"
              v-model="site.crawlTimeLimit"
              placeholder="Max Time of Crawl (Seconds)"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawlInterval" class="col-sm-2 col-form-label">Request Interval (Milli-seconds)</label>
          <div class="col-sm-10">
            <input
              :readonly='readOnly'
              name="crawlInterval"
              id="crawlInterval"
              v-model="site.crawlInterval"
              placeholder="Request Interval (Milli-seconds)"
              type="number"
              class="form-control"
            />
          </div>
        </div>
      </form>
    </div>

    <template v-slot:modal-header="{ cancel }" >
      <h5 class="modal-title">{{ title }}</h5>
      <div>
        <b-button v-show="currentAction!=='add'" variant="danger" size="lg" class="icon-button" @click="deleteSite"><i class="pe-7s-trash"/></b-button>
        <button type="button" aria-label="Close" @click="cancel" class="close">×</button>
      </div>
    </template>

    <template v-slot:modal-footer="{ ok, cancel }">
      <!-- Emulate built in modal footer ok and cancel button actions -->
      <b-button v-show="currentAction==='add'" variant="success" @click="createNew()">Create</b-button>
      <b-button v-show="currentAction==='edit'" variant="success" @click="update()">Save</b-button>
      <b-button v-show="currentAction==='view'" variant="success" @click="edit()">Edit</b-button>
      <b-button v-show="readOnly" variant="danger" @click="cancel()">Close</b-button>
      <b-button v-show="!readOnly" variant="danger" @click="cancel()">Cancel</b-button>
    </template>
  </b-modal>
</template>

<script>
import SiteService from '@/services/SiteService';

export default {
  name: 'SiteDetails',
  props: {
    site: {
      type: Object,
      required: false
    },
    action: {
      type: String,
      required: false
    }
  },
  watch: {
    status: function (current, previous) {
      this.currentStatus = current;
    },
    action: function (current, previous) {
      this.currentAction = current;
    },
    site: function (current, previous) {
      this.siteDetails = current;
    }
  },
  data() {
    return {
      siteDetails: this.site,
      currentAction: this.action
    };
  },
  computed: {
    title() {
      console.log('GENERATING TITLE ', this.currentAction);
      if (this.currentAction === 'add') return 'Register New Site';
      else if (this.currentAction === 'edit') return 'Edit Site';
      else return this.siteDetails.name;
    },
    readOnly() {
      return this.currentAction === 'view';
    }
  },
  methods: {
    edit() {
      this.currentAction = 'edit';
    },
    close() {
      this.currentStatus = null;
      this.$emit('Close');
    },
    show() {
      console.log('showing', this.siteDetails);
      console.log('this.action', this.action);
      // this.site = this.siteDetails;
      // this.currentAction = this.action;
    },
    save() {
      this.currentStatus = null;
      if (this.currentAction === 'add') this.$emit('contactAddded');
      else this.$emit('contactSaved');
    },
    HideStatus() {
      this.currentStatus = null;
    },
    createNew() {
      console.log('creating a new site:', this.siteDetails);
      SiteService.createNewSite(this.siteDetails)
      .then(respones => {
          console.log('created');
      })
      .catch(err => {
          console.log('failed to create');
      });
    },
    update() {
      console.log('Updating existing site:', this.siteDetails);
      SiteService.updateSite(this.siteDetails)
      .then(respones => {
        console.log('updated');
      })
      .catch(err => {
        console.log('failed to update');
      });
    },
    deleteSite() {
      console.log('Deleting site:', this.siteDetails.id);
      SiteService.deleteSite(this.siteDetails.id)
      .then(respones => {
        console.log('deleted');
      })
      .catch(err => {
        console.log('failed to update');
      });
    }
  }
};
</script>

<style>
.icon-button {
  padding: 2px 8px;
}
</style>