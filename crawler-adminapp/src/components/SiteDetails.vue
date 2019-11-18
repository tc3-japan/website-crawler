<template>
  <b-modal id="site-details" ref="modal" :title="title" size="xl" scrollable @shown="onShown" @hide="onHide">
    <div class="card-body">
      <b-alert 
        :show="status.message"
        dismissible
        v-model="status.visible"
        :variant="status.type">
            {{ status.message }}
      </b-alert>
      <form class>
        <div class="position-relative row form-group">
          <label for="name" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_NAME') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="name"
              id="name"
              v-model="siteDetails.name"
              :placeholder="$t('SITE_DETAILS_NAME_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.name.$error}"
            />
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.name.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_NAME_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="description" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_DESCRIPTION') }}</label>
          <div class="col-sm-10">
            <textarea 
              :readonly="readOnly" 
              name="description" 
              id="description" 
              v-model="siteDetails.description" 
              class="form-control"
              :placeholder="$t('SITE_DETAILS_DESCRIPTION_PLACEHOLDER')"
            ></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="url" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_URL') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="url"
              v-model="siteDetails.url"
              :placeholder="$t('SITE_DETAILS_URL_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.url.$error}"
            />
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.url.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_URL_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="content_url_patterns" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_URL_PATTERN') }}</label>
          <div class="col-sm-10">
            <textarea
              :readonly="readOnly"
              name="content_url_patterns"
              id="content_url_patterns"
              v-model="siteDetails.content_url_patterns"
              :placeholder="$t('SITE_DETAILS_URL_PATTERN_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.content_url_patterns.$error}"
            ></textarea>
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.content_url_patterns.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_URL_PATTERNS_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="category_extraction_pattern" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_CATEGORY_PATTERN') }}</label>
          <div class="col-sm-10">
            <textarea
              :readonly="readOnly"
              name="category_extraction_pattern"
              id="category_extraction_pattern"
              v-model="siteDetails.category_extraction_pattern"
              :placeholder="$t('SITE_DETAILS_CATEGORY_PATTERN_PLACEHOLDER')"
              type="text"
              class="form-control"
            ></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="content_selector" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_SELECTORS') }}</label>
          <div class="col-sm-10">
            <textarea 
              :readonly="readOnly" 
              name="content_selector" 
              id="content_selector" 
              v-model="siteDetails.content_selector" 
              class="form-control"
              :placeholder="$t('SITE_DETAILS_SELECTORS_PLACEHOLDER')"
            ></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="supports_robots_txt" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_ROBOTS') }}</label>
          <div class="col-sm-10">
            <toggle-button 
              :disabled="readOnly"
              name="supports_robots_txt"
              id="supports_robots_txt"
              :width="65"
              :height="35"
              v-model="siteDetails.supports_robots_txt"
              :labels="true"
              :color="{checked: '#28a745', unchecked: '#434055'}"
              :font-size="14"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_max_depth" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_DEPTH') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawl_max_depth"
              id="crawl_max_depth"
              v-model="siteDetails.crawl_max_depth"
              :placeholder="$t('SITE_DETAILS_DEPTH_PLACEHOLDER')"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_time_limit" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_MAX_TIME') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawl_time_limit"
              id="crawl_time_limit"
              v-model="siteDetails.crawl_time_limit"
              :placeholder="$t('SITE_DETAILS_MAX_TIME_PLACEHOLDER')"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_interval" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_INTERVAL') }}</label>
          <div class="col-sm-10">
            <input
              :readonly='readOnly'
              name="crawl_interval"
              id="crawl_interval"
              v-model="siteDetails.crawl_interval"
              :placeholder="$t('SITE_DETAILS_INTERVAL_PLACEHOLDER')"
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
      <b-button v-show="currentAction==='add'" variant="success" @click="createNew()">{{ $t('CREATE_BUTTON') }}</b-button>
      <b-button v-show="currentAction==='edit'" variant="success" @click="update()">{{ $t('SAVE_BUTTON') }}</b-button>
      <b-button v-show="currentAction==='view'" variant="success" @click="edit()">{{ $t('EDIT_BUTTON') }}</b-button>
      <b-button v-show="readOnly" variant="danger" @click="cancel()">{{ $t('CLOSE_BUTTON') }}</b-button>
      <b-button v-show="!readOnly" variant="danger" @click="cancel()">{{ $t('CANCEL_BUTTON') }}</b-button>
    </template>
  </b-modal>
</template>

<script>
import SiteService from '@/services/SiteService';
import { required } from 'vuelidate/lib/validators';

const defaultValues = {
  crawl_max_depth: 10,
  crawl_time_limit: 600,
  crawl_interval: 1000
};

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
    action: function (current, previous) {
      this.currentAction = current;
    },
    site: function (current, previous) {
      // Make a copy of the site object so that unsaved changes 
      // do no affect the sites list
      this.siteDetails = JSON.parse(JSON.stringify(current));
    },
    siteDetails : {
      handler(current, previous)
      {
        // Only set dirty for changes after the initial siteDetails has been set
        if (this.initialValue)
          this.initialValue = false;
        else 
          this.dirty = true;
      },
      deep: true
    }
  },
  data() {
    return {
      siteDetails: JSON.parse(JSON.stringify(this.site)),
      currentAction: this.action,
      status : {},
      dirty : false,
      initialValue : false,
      hiding: false
    };
  },
  computed: {
    title() {
      // Determine title of the modal based on the action being performed: add, edit or view
      switch(this.currentAction) {
        case 'add':
          return this.$t('SITE_DETAILS_ADD');
        case 'edit': 
          return this.$t('SITE_DETAILS_EDIT');
        default:
          return this.siteDetails.name;
      }
    },
    readOnly() {
      return this.currentAction === 'view';
    }
  },
  methods: {
    edit() {
      this.currentAction = 'edit';
    },
    hide() {
      this.hiding = true;
      this.$refs.modal.hide();
    },
    onHide(bvModalEvt) {
      // Avoid the close event handler being triggered twice, in case of confirmation
      if (this.hiding)
        return this.hiding = false;

      // If the user made changes to the site details, show a confirmation before closing
      if ((this.currentAction === 'edit' || this.currentAction === 'add') && this.dirty) {
        bvModalEvt.preventDefault();
        this.$bvModal.msgBoxConfirm(this.$t('SITE_CANCEL_UPDATE_CONFIRMATION'), 
        { 
          title : this.$t('SITE_CANCEL_UPDATE_CONFIRMATION_TITLE'),
          okTitle: this.$t('YES_BUTTON'),
          cancelTitle: this.$t('NO_BUTTON'),
        })
        .then((confirmationResponse) => {
          if (confirmationResponse === true) {
            this.$emit('Close');
            this.hide();
            this.$v.$reset();
            this.dirty = false;
          }
        });
      }
      else {
        this.$emit('Close');
        this.$v.$reset();
      }
    },
    onShown() {
      this.currentAction = this.action;
      
      if (this.currentAction === "add")
        this.siteDetails = Object.assign({}, defaultValues);
      else
        this.siteDetails = JSON.parse(JSON.stringify(this.site));

      this.dirty = false;
      this.initialValue = true;
    },
    createNew() {
      // Tell vuelidate to perform validation check
      this.$v.siteDetails.$touch();
      // Cancel save if there are error values
      if(this.$v.siteDetails.$error)
        return;

      // Display save confirmation dialog
      this.cofirmSave().then(confirmationResponse => {
        if (!confirmationResponse)
          return;
        
        // Pass the site details to the api
        SiteService.createNewSite(this.siteDetails)
        .then(respone => {
            this.$emit('sites-updated', { message : this.$t('SITE_CREATE_SUCCESS'), type : 'success' });
            this.hide();
        })
        .catch(err => {
            this.displayErrorResponse(err);
        });
      });
    },
    update() {
      // Tell vuelidate to perform validation check
      this.$v.siteDetails.$touch();
      // Cancel update if there are error values
      if(this.$v.siteDetails.$error)
        return;

      this.cofirmSave().then(confirmationResponse => {
        if (!confirmationResponse)
          return;

        // Pass the updated site details to the api
        SiteService.updateSite(this.siteDetails)
        .then(respone => {
          this.$emit('sites-updated', { message : this.$t('SITE_UPDATE_SUCCESS'), type : 'success' });
          this.hide();
        })
        .catch(err => {
          this.displayErrorResponse(err);
        });
      });
    },
    deleteSite() {
      // Show confirmation dialog before saving
      this.$bvModal.msgBoxConfirm(this.$t('SITE_DELETE_CONFIRMATION', { filename : this.siteDetails.name }), 
      { 
        title : this.$t('SITE_DELETE_CONFIRMATION_TITLE'),
        okTitle: this.$t('YES_BUTTON'),
        cancelTitle: this.$t('NO_BUTTON'),
      })
      .then((confirmationResponse) => {

        if (confirmationResponse === false)
          return;

        // Send delete request to api
        SiteService.deleteSite(this.siteDetails.id)
        .then(response => {
          this.$emit('sites-updated', { message : this.$t('SITE_DELETE_SUCCESS'), type : 'success' });
          this.hide();
        })
        .catch(err => {
          this.displayErrorResponse(err);
        });
      });
    },
    cofirmSave() {
      return this.$bvModal.msgBoxConfirm(this.$t('SITE_SAVE_CONFIRMATION'), 
      { 
        title : this.$t('SITE_SAVE_CONFIRMATION_TITLE'),
        okTitle: this.$t('YES_BUTTON'),
        cancelTitle: this.$t('NO_BUTTON'),
      });
    },
    setStatus(message, type) {
      this.status = { message : message, type : type, visible : true };
    },
    displayErrorResponse(error, fallbackMessageKey) {
      let messageKey = (error.response && error.response.data && error.response.data.message) ? error.response.data.message : fallbackMessageKey;
      let message = this.$t(messageKey || 'CRUD_GENERIC_FAILURE');
      this.setStatus(message, 'danger');
    }
  },
  validations: {
    siteDetails :{
      name: {
        required
      },
      url: {
        required
      },
      content_url_patterns : {
        required
      }
    }
}
};
</script>

<style scoped>

.icon-button {
  padding: 2px 8px;
}

.invalid-feedback {
  display: block;
  font-size: 1.0em;
}

</style>