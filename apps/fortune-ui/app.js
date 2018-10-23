//Need to check for Backend Service
var url
$.ajax({
        url: "http://" + window.location.hostname + "/backend",
        cache: false,
        statusCode: {
            200: function(data) {
              console.log("Successful config lookup!")
              url = data;
            },
            404: function() {
              console.log("Error on config lookup!")
              url = "http://" + window.location.hostname + ":9080"
            }
          },
        async: false
    });

console.log("Backend URL: " + url);

function loadOne() {
    console.log("Getting fortunes");
    $.ajax({
        url: url + "/fortune-backend-jee/app/fortune/random"
    }).then(function(data) {
        var container = $('#fortune');
        console.log("Adding Fortune: " + data);
        if(data) {
            container.append("<p>" + data.text + "</p>");
        } else {
            container.append("<p>You future is murky...</p>");
        }
    });
}

function loadAll() {
    console.log("Getting all fortunes");

    $.ajax({
        url: url + "/fortune-backend-jee/app/fortune/all"
    }).then(function(data) {
        var container = $('#fortune');
        console.log("Adding Fortunes: " + data);
        container.empty();
        if(data) {
            $.each(data, function(index, value) {
                console.log("Adding fortune " + index);
                container.append("<p>" + value.text + "</p><br>");
            });
        } else {
            container.append("<p>-- Empty --</p>");
        }
    });
}

function handle(event) {
    if (event.keyCode == 13) {
        event.preventDefault();
        var dataObject = new Object();
        dataObject.text = input_fortune.value;
        console.log("Data: " + dataObject);
        $.ajax({
            url: url + "/fortune-backend-jee/app/fortune",
            method: "PUT",
            data: JSON.stringify(dataObject),
            dataType: 'json',
            contentType:"application/json; charset=utf-8",
            success: function () {
                location.reload();
            }
        });
    }
};
