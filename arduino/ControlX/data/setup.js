var request = new XMLHttpRequest();

function applyChanges()
{
  let ssid = document.getElementById('ssidTb').value;
  let pass = document.getElementById('passPb').value;
  if (ssid == '' || pass == '')
  {
    alert("Incorrect data!");
    return;
  }
  request.open('POST', '/setup?ssid=' + ssid + '&pass=' + pass);
  request.send();
  alert("Changes applied!");
}
