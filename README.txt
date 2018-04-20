Android平台下的富文本解析器

1.支持自定义html标签
2.支持各种标签中style属性的解析


使用方法：
String content = "<p><span style=\"text-decoration: underline;\">测试</span></p>";

//记住要加上html，body的标签不然会异常
content = "<html><body>" + content + "</body></html>";
Spanned s = UPHtmlTagHandler.fromHtml(content,null,new UPExtendTagHandler(this,tv.getTextColors()));
tv.setText(s);

demo项目，不定时会完善

