var initFinish = false;
var canSend = false;
var isWaiting = false;

var helperName = "Luguanxing's Helper"
var helperImageUrl = './images/helper.png';
var botName = 'AI Assistant'
var botImageUrl = './images/bot.png';
var clientName = 'You';
var clientImageUrl = './images/client.png';


function delay(time) {
    return new Promise(resolve => setTimeout(resolve, time));
}

function init() {
    delay(parseInt(Math.random() * 1000))
        .then(() => {
            helperReply("您好，欢迎光临！<br/>Hello and welcome!");
            return delay(parseInt(1000 + Math.random() * 1000));
        })
        .then(() => {
            helperReply("正在为您接入AI助手...<br/>Connecting you to the AI assistant now...");
            return delay(parseInt(1000 + Math.random() * 1000));
        })
        .then(() => {
            aiReply("您好，现在您可以问我关于Luguanxing的相关资讯（内容可参考左下角📚图标），我只会回答相关的问题。<br>Hello, you can now ask me about Luguanxing-related information (please refer to the 📚 icon in the bottom left corner). I will only answer questions related to this topic.<br/>");
            return delay(Math.random() * 1000);
        })
        .then(() => {
            initFinish = true;
            updateButtonStatus();
        })
}

function helperReply(str) {
    var html = "<div class='reply'><div class='msg'>" +
        "<img src=" + helperImageUrl + " />" +
        "<span class='name'>" + helperName + "</span>" +
        "<p><i class='msg_output'></i>" + str + "</p>" +
        "</div></div>";
    refeshView(html);
}

function aiReply(str) {
    var html = "<div class='reply'><div class='msg'>" +
        "<img src=" + botImageUrl + " />" +
        "<span class='name'>" + botName + "</span>" +
        "<p><i class='msg_output'></i>" + str + "</p>" +
        "</div></div>";
    refeshView(html);
}


function showQuestion(str) {
    var html = "<div class='ask'><div class='msg'>" +
        "<img src=" + clientImageUrl + " />" +
        "<p style='background-color: #9eeb6b;'><i class='msg_input'></i>" + str +
        "</p></div></div>";
    refeshView(html);
}

function refeshView(html) {
    let message = $('#message');
    message.append(html);
    return $('html,body').animate({
        scrollTop: message.outerHeight() - window.innerHeight
    }, 0);
}

function send() {
    if (canSend) {
        var input = $("#footer .my-input");
        var question = input.val();
        input.val('');
        showQuestion(question);
        askAi(question);
        updateButtonStatus();
    }
}

function askAi(question) {
    $('#loading').css('display', 'flex').fadeIn('fast');
    isWaiting = true;
    $.ajax({
        url: contextPath + '/askQuestion',
        type: 'POST',
        data: JSON.stringify({question: question}),
        contentType: 'application/json',
        success: function (response) {
            if (response.role == 0) {
                helperReply(response.answer);
            } else {
                aiReply(response.answer);
            }
            updateButtonStatus();
        },
        error: function (error) {
            console.log(error);
            helperReply("服务器出错了，请稍后再试。<br/>Sorry, the server is down. Please try again later.");
        }
        ,complete: function () {
            isWaiting = false;
            $('#loading').fadeOut('fast');
        }
    });
}

function updateButtonStatus() {
    if ($("#footer .my-input").val().length > 0 && initFinish && !isWaiting) {
        enableButton();
    } else {
        disableButton();
    }
}

function enableButton() {
    $('.send').css('background', 'rgb(51 126 212)').prop('disabled', false);
    canSend = true;
}

function disableButton() {
    $('.send').css('background', '#ddd').prop('disabled', true);
    canSend = false;
}

$(function () {
    $('#footer').on('keyup', 'input', function () {
        updateButtonStatus();
    })

    $('#footer .send').click(send)

    $("#footer .my-input").keydown(function (e) {
        if (e.keyCode == 13) {
            return send();
        }
    });

    init();
})
