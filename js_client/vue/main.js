const learningType = require("./learning-type.vue");
const queryTemplates = require("./query-templates.vue");
const slaSelection = require("./sla-selection.vue");
const slaRecs = require("./sla-recs.vue");
const strategy = require("./strategy.vue");
const liveDisplay = require("./liveDisplay.vue");

module.exports = {
    data: function () {
        return {
            templates: [],
            frequencies: [],
            mode: false,
            deadline: false,
            selectedSLA: false
        };
    },

    "name": "wisdom-main",
    
    components: {
        learningType, queryTemplates, slaSelection, slaRecs, strategy, liveDisplay
    },

    methods: {
        templatesChanged: function(t) {
            this.templates = t;
            this.frequencies = false;
            this.deadline = false;
        },

        modeChanged: function(m) {
            console.log(m + ", " + (m == 'rlearn'));
            this.mode = m;
        },

        deadlineChanged: function(d) {
            this.deadline = d;
        },

        frequenciesChanged: function(f) {
            this.frequencies = f;
        },

        selectedSLAChanged: function (sla) {
            this.selectedSLA = sla;
        }
    }

};
