1、工程可以直接引入这个CoreD Module,然后Common模块也会被引入，需要注意广告需求选择对应的聚合，
把不需要的聚合进行删除
2、so.txt 需要全部重新修改成
需要按照差异化文档(https://stayfoolish.feishu.cn/wiki/M3iwwLkP0iQcXRkak7icr1FonQc)的要求进行修改
3、Todo 标识的地方必须要修改、其他地方有疑问的可以问黄天云
4、所有的类名、方法名都需要修改，注意如果起简单的a.a可能会与外面的混淆后的包冲突所以起名字的时候需要注意一下；
5、so加解密直接用我的方法在 CoreD module 下的test类中
6、dex的加解密需要自己去实现，实现后记得加入垃圾代码。且每个包都需要做差异化，这一块最好做的在细一点。
7、外部差异化不能将所有的功能都集中在一个类中或方法中
8、对同样的一张图片进行修改md5值修改地址https://www.strerr.com/cn/md5_edit.html