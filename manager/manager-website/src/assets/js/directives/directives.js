/**
 * global directives
 */

// e.g.
export const btnPermission = {
    inserted(el, bindings, vnode) {
        const value = bindings.value;

        // permission
        const flag = vnode.context.$store.state.security.btnPermission[value];

        // has no permission, remove it
        !flag && el.parentNode.removeChild(el);
    },
};
