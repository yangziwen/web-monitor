let util = {

};
util.title = function (title) {
    title = title ? title + ' - Home' : 'Web监控系统原型';
    window.document.title = title;
};

export default util;