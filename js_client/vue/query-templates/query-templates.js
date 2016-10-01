const axios = require("axios");

module.exports = {
    data: function () {
        return {
            templates: [],
            selected: [],
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
            this.$emit("templates-changed", this.selected);
        },

        clear: function() {
            this.saved = false;
        },

        countSelected: function () {
            return this.selected
                .filter(x => x != null)
                .length;
        }
    },
    
    created: function () {
        this.getTemplates();
    }
};
