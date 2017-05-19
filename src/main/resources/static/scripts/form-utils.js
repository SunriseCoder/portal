var FormUtils = {
    submitForm: function(formId) {
        var form = document.getElementById(formId);
        form.submit();
    },

    submitOnEnterPressed: function(event, formId) {
        if (event.keyCode == 13) {
            FormUtils.submitForm(formId);
        }
    },

    generatePassword: function(target) {
        var pass = Math.random().toString(36).slice(-8);
        document.getElementById(target).value = pass;
    }
}
