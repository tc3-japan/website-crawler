module.exports = {
    devServer: {
	// proxy: 'http://ae91f8bcb014711ea9701063bddf76ed-904913025.ap-northeast-1.elb.amazonaws.com',
	proxy: 'http://localhost:8090',
        disableHostCheck: true
    }
};
