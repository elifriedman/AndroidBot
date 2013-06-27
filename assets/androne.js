var began = false;

var stControl = function() {
    if(!began) {
        began = true;
        $("#btn_ctrl").val("Stop").button("refresh");
        document.body.addEventListener("keydown",move);
        document.body.addEventListener("keyup",move);
    } else {
        began = false;
        $("#btn_ctrl").val("Begin").button("refresh");
        document.body.removeEventListener("keydown",move);
        document.body.removeEventListener("keyup",move);
        send(0,0);
    }
}
var y = 0, x = 0;
var LEFT = 37, A = 65;
var UP = 38, W = 87;
var RIGHT = 39, D = 68;
var DOWN = 40, S = 83;
function move(evt) {
    var key = evt.keyCode;
    var changer;
    var y_old = y;
    var x_old = x;
    
    if(evt.type=="keydown") {
        changer = 1;
    } else if(evt.type=="keyup") {
        changer = 0;
    }
    
    switch(key) {
        case LEFT:
        case A:
            if(x_old <= 0)
                x = -1*changer;
            break;
        case UP:
        case W:
            if(y_old >= 0)
                y = changer;
            break;
        case RIGHT:
        case D:
            if(x_old >= 0)
                x = changer;
            break;
        case DOWN:
        case S:
            if(y_old <= 0)
                y = -1*changer;
            break;
    }
    
    if(y==y_old && x==x_old) //If nothing has changed, stop.
        return;
    $("#debug_msg").html("("+x+","+y+")");
    send(x,y);
}
function send(x,y) {
    $.ajax({
            type: "GET",
            url: basicURL + data,
            data: "x="+x+"&y="+y,
            cache: false,
            error: function(jqXHR, txtStat, errorThrown) {
               // $("#debug_msg").html("Oh no: " + txtStat + "!!");
                }
        });
}
