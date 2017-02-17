const axios = require("axios");
const deepcopy = require("deepcopy");

module.exports = {
    data: function () {
        return {
            waiting: true,
            results: false,
            selectedSLA: false,
            sentRequest: false,
            
            saved: false,
            frequencies: [5,5,5,5,5,5,5,5,5,5,5]
        };
    },

    props: ["mode", "templates", "sla"],

    computed: {
        slaSelected: function() {
            return this.correctMode() && this.selectedSLA;
        },

        canSave: function () {
            return this.selectedSLA != false && !this.saved;
        }
    },

    methods: {
        correctMode: function () {
            return (this.mode == "slearn")
                && this.sla != false
                && this.frequencies.length != 0;
        },

        selectSLA: function(idx) {
            if (idx == -1) {
                // select the original SLA
                this.selectedSLA = this.results.original;
                this.selectedSLA.index = -1;
                return;
            }

            this.selectedSLA = this.results.suggestions[idx];
            this.selectedSLA.index = idx;
        },

        checkMode: function() {
            if (!this.correctMode()) {
                this.results = false;
                this.waiting = true;
                this.selectedSLA = false;
                this.sentRequest = false;
                this.clear();
                return;
            }

            if (this.sentRequest)
                return;
            
            this.sentRequest = true;

            // we are now in the correct mode. send the request...
            axios.post("/slarecs",
                       { "templates": this.templates,
                         "deadline": this.sla,
                         "frequencies": this.getFreqs() })
                .then(res => {
                    this.waiting = false;
                    this.results = res.data;
                });
        },

        getFreqs: function() {
            return this.frequencies
                .filter((itm, idx) => this.templates.indexOf(idx) > -1);
        },

        clear: function() {
            this.selectedSLA = false;
            this.saved = false;
            this.$emit("selected-sla-changed", false);
            this.$emit("frequency-changed", false);
        },
        
        save: function () {
            console.log("Session ID for frequency request: " + this.selectedSLA.sessionID);
            axios.post("/frequency",
                       {"sessionID": this.selectedSLA.sessionID,
                        "frequencies": this.getFreqs()})
                .then(() => {
                    this.saved = true;
                    this.$emit("selected-sla-changed", deepcopy(this.selectedSLA));
                });
            
        }
    },

    watch: {
        mode: function () { this.checkMode(); },
        sla: function() { this.checkMode(); },
        templates: function() { this.checkMode(); }
    },

    created: function() {
        this.checkMode();
    }


};
