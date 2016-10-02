const learningType = require("./learning-type.vue");
const queryTemplates = require("./query-templates.vue");
const slaSelection = require("./sla-selection.vue");
const slaRecs = require("./sla-recs.vue");

module.exports = {
    data: function () {
        return {
            templates: [],
            mode: false,
            deadline: false
        };
    },
    
    components: {
        learningType, queryTemplates, slaSelection, slaRecs
    },

    methods: {
        templatesChanged: function(t) {
            this.templates = t;
        },

        modeChanged: function(m) {
            this.mode = m;
        },

        deadlineChanged: function(d) {
            this.deadline = d;
        }
    }

};
