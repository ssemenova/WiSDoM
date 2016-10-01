const axios = require("axios");

module.exports = {
    data: function () {
        return {
            templates: [],
            queries: [],
            saved: false
        };
    },

    methods: {
        getTemplates: function () {
            axios.get("/querytemplates")
                .then((r) => {
                    this.templates = r.data;
                });
        },
        
        save: function () {
            this.saved = true;
        },

        clear: function() {
            this.saved = false;
        },

        countSelected: function () {
            return this.queries
                .filter(x => x != null)
                .length;
        }
    },
    
    created: function () {
        this.getTemplates();
    }
};
