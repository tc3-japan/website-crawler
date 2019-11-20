var express = require('express');
app = express();

var serveStatic = require('serve-static');
app.use(serveStatic(__dirname + "/dist"));

var port = process.env.PORT || 80;

app.listen(port, () => {
    console.log(`Server is listening on port ${port}`);
});