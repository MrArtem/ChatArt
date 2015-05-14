var messageOption = function (text, user, value) {
    return {
        message: text,
        user: user,
        id: value
    };
};
var infoLogin = function (name, surname) {
    return {
        name: name,
        surname: surname
    };
};
var appState = {
    mainUrl: 'chat',
    token: 'TE11EN'
};
var listForSaving = [];
var deleteIconUtfCode = '\u2421';
var changeIconUtfCode = '\u270e';
function run() {
	
    var appContainerSend = document.getElementById('send');
    var appContainerDelete = document.getElementById('delete');
    var appContainerSelect = document.getElementById('allMessages');
    var appContainerEnterMessage = document.getElementById('sendText');
    var appContainerServer = document.getElementById('server');

    restoreMessages();
    updateMessages();

    appContainerSend.addEventListener('click', delegateEventSend);
    appContainerDelete.addEventListener('click', delegateEventDelete);
    appContainerSelect.addEventListener('click', delegateEventSelect);
    appContainerServer.addEventListener('click', delegateEventServer);
    appContainerEnterMessage.addEventListener('keypress', delegateEventEnterMessage);

    document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight;
}
function delegateEventSend(evtObj) {
    var index = document.getElementById("allMessages").selectedIndex;
    if (index == -1) {
        var text = document.getElementById('sendText');
        var name = document.getElementById('name');
        var surname = document.getElementById('surname');
        if (text.value && name.value && surname.value) {

            var select = document.getElementById("allMessages");
            var option = document.createElement("option");

            var sendMessage = messageOption(text.value, surname.value + " " + name.value, select.length);

            storeMessages(sendMessage, function () {
                document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight+19;
            });
           
            text.value = "";
            var scrolbar = document.getElementById("allMessages");
            document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight+19;
        }
    } else {
        var sendText = document.getElementById('sendText');
        if (sendText.value != "") {
            var name = document.getElementById('name');
            var surname = document.getElementById('surname');

            var index = document.getElementById("allMessages").selectedIndex;
            var select = document.getElementById("allMessages")[index];
            
            var changeMessage = messageOption(sendText.value, surname.value + " " + name.value, index);
            changeMessages(changeMessage, function () {
                document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight;
            });
           
            select.selected = false;
            sendText.value = null;
            document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight;
        }
    }
}
function delegateEventEnterMessage(evtObj) {
    if (evtObj.keyCode == "13") {
        delegateEventSend(evtObj)
    }
}
function delegateEventDelete(evtObj) {
    var sendText = document.getElementById('sendText');

    var name = document.getElementById('name');
    var surname = document.getElementById('surname');

    var index = document.getElementById("allMessages").selectedIndex;
    var select = document.getElementById("allMessages")[index];

    deleteMessage(index, function () {
    });
    
    sendText.value = "";
    select.selected = false;
}
function delegateEventSelect(evtObj) {
    var sendText = document.getElementById('sendText');
    var index = document.getElementById("allMessages").selectedIndex;
    var select = document.getElementById("allMessages")[index];

    var subindex = select.text.indexOf(":");
    var name = document.getElementById('name').value;
    var surname = document.getElementById('surname').value;
    var nameAndSurename = select.text.substring(0, subindex - 1);
    var myNameAndSurename = surname + " " + name;

    if (select.text != nameAndSurename+" : message has deleted." && myNameAndSurename == nameAndSurename) {
        if (select.text.indexOf(changeIconUtfCode) != -1) {
            sendText.value = select.text.substring(subindex + 2, select.text.indexOf(changeIconUtfCode));
        } else {
            sendText.value = select.text.substring(subindex + 1);
        }
    } else {
        select.selected = false;
    }
}

function delegateEventServer(evtObj) {
    $("#server").removeClass('btn btn-danger');
    $("#server").addClass('btn btn-success');
}
function storeMessages(sendMessage, continueWith) {
    post(appState.mainUrl, JSON.stringify(sendMessage), function () {
        updateMessages();
    });
}
function storeInfoLogin(infoLogin) {
    if (typeof (Storage) == "undefined") {
        alert('localStorage is not accessible');
        return;
    }
    localStorage.setItem("Login info", JSON.stringify(infoLogin));
}
function restoreMessages(continueWith) {
    var url = appState.mainUrl + '?token=' + appState.token;

    get(url, function (responseText) {
        console.assert(responseText != null);
        delegateEventServer();
        var response = JSON.parse(responseText).messages;
        createAllMessages(response);

        continueWith && continueWith();
    });
    document.getElementById("allMessages").scrollTop = document.getElementById("allMessages").scrollHeight;
}
function updateMessages(continueWith) {
    var url = appState.mainUrl + '?token=' + appState.token;

    get(url, function (responseText) {
        console.assert(responseText != null);
        delegateEventServer();
        var response = JSON.parse(responseText).messages;
        for (var i = 0; i < response.length; i++) {
            var message = response[i];
            if (message.requst == "POST") {
                addAllMessages(message);
            }
            if (message.requst == "PUT") {
                addChangeMessage(message);
            }
            if (message.requst == "DELETE") {
                addDeleteMessage(message);
            }
        }

        continueWith && continueWith();
    });
    setTimeout(updateMessages, 1000);
}
function restoreLoginInfo() {
    if (typeof (Storage) == "undefined") {
        alert('local storage is not accessible');
        return;
    }
    var item = localStorage.getItem("Login info");
    return item && JSON.parse(item);
}
function createAllMessages(allMessages) {
    for (var i = 0; i < allMessages.length; i++) {
        addAllMessages(allMessages[i]);
    }
}
function addAllMessages(message) {
    if (listForSaving[message.id] == null) {
        var select = document.getElementById('allMessages');
        var option = document.createElement("option");
        option.text = message.user + " : " + message.message;
        option.value = message.id;
        listForSaving.push(message);
        select.add(option);
    }
}
function addChangeMessage(message) {
    if (listForSaving[message.id] != null) {
        var select = document.getElementById("allMessages")[message.id];
        select.text = message.user + " : " + message.message + changeIconUtfCode;
        listForSaving[message.id] = message;
    }
}
function addDeleteMessage(message) {
    if (listForSaving[message.id] != null) {
        var select = document.getElementById("allMessages")[message.id];
        select.text = message.user + " : " + message.message;
        listForSaving[message.id] = message;
    }
}
function ActiveInfoLogin() {
    var infoLogin = restoreLoginInfo();
    var name = document.getElementById('name');
    var surname = document.getElementById('surname');

    name.value = infoLogin.name;
    surname.value = infoLogin.surname;
}
function LogOutFromChat() {
    storeInfoLogin(null);
}
function changeMessages(changeMessage, continueWith) {
    put(appState.mainUrl, JSON.stringify(changeMessage), function () {
        updateMessages();
    });
}
function deleteMessage(index,continueWith) {
 var indexToken = index*8+11; 
 var url = appState.mainUrl + '?token=' + "TN" +indexToken.toString() + "EN";
    del(url, function () {

     continueWith && continueWith();
    });
}
$(document).ready(function () {
    var isNotLogin = restoreLoginInfo() == null;

    var dialog = $('#Login').dialog({
        title: 'Login',
        modal: true,
        resizable: false,
        autoOpen: isNotLogin,

        buttons: {
            SignIn: function () {
                var inputName = document.getElementById('inputName');
                var inputSurName = document.getElementById('inputSurName');

                var name = document.getElementById('name');
                var surname = document.getElementById('surname');

                if (inputName.value != "" && inputSurName != "") {
                    name.value = inputName.value;
                    surname.value = inputSurName.value;
                    inputName.value = null;
                    inputSurName.value = null;

                    storeInfoLogin(infoLogin(name.value, surname.value));
                    $(this).dialog("close");
                } else {
                    alert("Enter name or surname !")
                }
            }
        }
    });
    if (isNotLogin == false) {
        ActiveInfoLogin();
    }
    $(document).on('click', '#logOut', function () {

        LogOutFromChat();

        var name = document.getElementById('name');
        var surname = document.getElementById('surname');
        name.value = null;
        surname.value = null;
        dialog.dialog('open');

    });
});

$(function () {
    $("#RenameDiv").dialog({
        autoOpen: false,
        show: {
            effect: "blind",
            duration: 1000
        },
        hide: {
            effect: "explode",
            duration: 1000
        },
        buttons: {
            OK: function () {
                var name = document.getElementById('name');
                var surname = document.getElementById('surname');

                var changeName = document.getElementById('changeName');
                var changeSurname = document.getElementById('changeSurname');
                if (changeName.value != "" && changeSurname.value != "") {
                    name.value = changeName.value;
                    surname.value = changeSurname.value;
                    changeName.value = null;
                    changeSurname.value = null;
                    storeInfoLogin(infoLogin(name.value, surname.value));

                    $(this).dialog("close");
                }
            }
        }
    });

    $("#rename").click(function () {
        var changeName = document.getElementById('changeName');
        var changeSurname = document.getElementById('changeSurname');

        var name = document.getElementById('name');
        var surname = document.getElementById('surname');

        changeName.value = name.value;
        changeSurname.value = surname.value;
        $("#RenameDiv").dialog("open");
    });
});


function defaultErrorHandler(message) {
    $("#server").removeClass('btn btn-success');
    $("#server").addClass('btn btn-danger');
}

function get(url, continueWith, continueWithError) {
    ajax('GET', url, null, continueWith, continueWithError);
}
function post(url, data, continueWith, continueWithError) {
    ajax('POST', url, data, continueWith, continueWithError);
}
function put(url, data, continueWith, continueWithError) {
    ajax('PUT', url, data, continueWith, continueWithError);
}
function del(url, continueWith, continueWithError) {
    ajax('DELETE', url, null, continueWith, continueWithError);
}
function isError(text) {
    if (text == "")
        return false;

    try {
        var obj = JSON.parse(text);
    } catch (ex) {
        return true;
    }

    return !!obj.error;
}
function ajax(method, url, data, continueWith, continueWithError) {
    var xhr = new XMLHttpRequest();

    continueWithError = continueWithError || defaultErrorHandler;
    xhr.open(method || 'GET', url, true);

    xhr.onload = function () {
        if (xhr.readyState !== 4)
            return;

        if (xhr.status != 200) {
            continueWithError('Error on the server side, response ' + xhr.status);
            return;
        }

        if (isError(xhr.responseText)) {
            continueWithError('Error on the server side, response ' + xhr.responseText);
            return;
        }

        continueWith(xhr.responseText);
    };

    xhr.ontimeout = function () {
        continueWithError('Server timed out !');
    }

    xhr.onerror = function (e) {
        var errMsg = 'Server connection error !\n' +
    	'\n' +
    	'Check if \n' +
    	'- server is active\n' +
    	'- server sends header "Access-Control-Allow-Origin:*"';

        continueWithError(errMsg);
    };

    xhr.send(data);
}
