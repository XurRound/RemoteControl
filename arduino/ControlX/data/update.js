function filechanged(ref)
{
	document.getElementById('filename').innerText = ref.files[0].name.toString();
}

function update()
{
	if (document.getElementById('uploader').files[0] == null)
		return;
	var formData = new FormData(document.forms.updateform);
	var request = new XMLHttpRequest();
	request.open("POST", "/update");
	request.send(formData);
	request.onload = () => alert(request.response);
	document.getElementById('main').setAttribute("hidden", "");
	document.getElementById('progress').removeAttribute("hidden");
	var interval = setInterval(decreaseTime, 1000);
	setTimeout(() => { clearInterval(interval); window.location = "/"; }, 30000);
}

var time = 30;

function decreaseTime()
{
	var percent = 100 - (time * 100 / 30);
	document.getElementById('progress-bar').style = `width: ${percent}%;`;
	document.getElementById('timer').innerText = `${time} sec`;
	time--;
}