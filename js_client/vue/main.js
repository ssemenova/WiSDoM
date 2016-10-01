const learningType = require("./learning-type.vue");
const queryTemplates = require("./query-templates.vue");
const slaSelection = require("./sla-selection.vue");

module.exports = {
    data: function () {
        return {
            templates: []
        };
    },
    
    components: {
        learningType, queryTemplates, slaSelection
    },

    methods: {
        templatesChanged: function(t) {
            this.templates = t;
        }
    }

};
