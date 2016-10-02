const axios = require("axios");
const deepcopy = require("deepcopy");

module.exports = {
    data: function () {
        return {
            templates: [],
            selected: [],
            saved: false
        };
    },

    methods: {
        selectedTemplates: function (){
            return this.selected
                .map((itm, idx) => (itm === true) && idx)
                .filter(itm => itm != false);
        },
        numSelected: function () {
            return this.selected
                .filter(x => x === true)
                .length;
        },
        getTemplates: function () {
            axios.get("/querytemplates")
                .then((r) => {
                    this.templates = r.data;
                });
        },
        
        save: function () {
            this.saved = true;
            this.$emit("templates-changed", deepcopy(this.selectedTemplates()));
        },

        clear: function() {
            this.saved = false;
        }
    },
    
    created: function () {
        this.getTemplates();
    }
};
