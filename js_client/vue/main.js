const learningType = require("./learning-type.vue");
const queryTemplates = require("./query-templates.vue");
const slaSelection = require("./sla-selection.vue");
const slaRecs = require("./sla-recs.vue");
const strategy = require("./strategy.vue");
const liveDisplay = require("./live-display.vue");
const slearnGraph = require("./slearn-graph.vue");
const rlearnGraph = require("./rlearn-graph.vue");

module.exports = {
    data: function () {
        return {
            templates: [],
            frequencies: [],
            mode: false,
            deadline: false,
            selectedSLA: false,
            slaType: "Max",
            rlearnData: []
        };
    },

    "name": "wisdom-main",

    components: {
        learningType, queryTemplates, slaSelection,
        slaRecs, strategy, liveDisplay, slearnGraph,
        rlearnGraph
    },

    methods: {
        templatesChanged: function(t) {
            this.templates = t;
            this.frequencies = false;
            this.deadline = false;
        },

        modeChanged: function(m) {
            this.mode = m;
        },

        deadlineChanged: function(d) {
            this.deadline = d;
            this.rlearnData = [];
        },

        frequenciesChanged: function(f) {
            this.frequencies = f;
        },

        selectedSLAChanged: function (sla) {
            console.log("Got new SLA: " + JSON.stringify(sla));
            this.selectedSLA = sla;
        },

        gotRLearnData: function (dataPoint) {
            this.rlearnData.push(dataPoint);
        },

        slaTypeChanged: function (t) {
            this.slaType = t;
        }
    }

};
