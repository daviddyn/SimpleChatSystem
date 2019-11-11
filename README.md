程序功能：允许你和你的计算机进行驴唇不对马嘴的对话。
开发语言：渣哇
开发环境：IntelliJ IDEA
JDK版本：11，但是我没用此版本的任何新特性，所以低版本应该也能编译运行。

总体实现方式：全部传统手段。
1-分词：自创的概率语言模型+逆向最大长度匹配混合算法
2-对话库检索：词频向量夹角余弦

特别说明：
未使用任何三方库，程序所有内容全部自己实现。

目录说明：
src - 文件夹 - 所有程序代码
chat_srcs - 文件夹，内包含若干txt文件 - 存储对话库的源文件，可以随心编辑，但编辑后别忘了重新编译才能生效(见后文[使用方法])。
random_srcs - 文件夹，内包含若干txt文件 - 存储随机答句的源文件，可以随心编辑，但编辑后别忘了重新编译才能生效(见后文[使用方法])。
specials_src.txt - txt文件 - 特殊词汇源文件，可以随心编辑，但编辑后别忘了重新编译才能生效(见后文[使用方法])。
thesaurus_src.txt - txt文件 - 同义词库源文件，可以随心编辑，但编辑后别忘了重新编译才能生效(见后文[使用方法])。
ChineseFreqDict - 文件 - 中文词典，程序默认读取此文件。
ChineseThesaurus - 文件 - 编译后的中文同义词库，程序默认读取此文件。
ChineseChats - 文件 - 编译后的中文对话库，程序默认读取此文件。
ChineseRandomResponses - 文件 - 编译后的随机答句库，程序默认读取此文件。

程序代码结构：
1. com.davidsoft.console包：里面是自己之前装好的一些控制台操作方法，便于控制台输入。
2. com.davidsoft.io包：里面是自己之前装好的一些IO操作方法，便于从流中读写二进制数据。
3. com.davidsoft.natural包：里面是封装好的NLP算法。
    3.1. com.davidsoft.natural中的类：都是一些抽象类或通用结构，哪国语言都适用。
    3.2. com.davidsoft.natural.chinese包：包括了专门处理中文的方法。
    3.3. com.davidsoft.natural.chinese.commands包：此包中的类名与对话库中的命令相对应，对话系统会通过Java的反射机制调用此包类中的方法实现答句生成。
4. com.davidsoft.simplechatsystem包：为本程序专用的代码，本程序的主函数在里面，主要调用com.davidsoft.natural包中的方法完成演示。

使用方法：
运行com.davidsoft.simplechatsystem.Main，根据提示完成以下操作：
a) 编译thesaurus_src.txt和thesaurus_src.txt两个源文件，生成ChineseThesaurus中文同义词词库文件。编译过程中会需要读取中文词典ChineseFreqDict。
b) 编译chat_srcs下的所有源文件，生成ChineseChats对话样本库文件。编译过程中会需要读取中文词典ChineseFreqDict和中文同义词词库ChineseThesaurus。
c) 编译random_srcs下的所有源文件，生成ChineseRandomResponses中文随机答句文件。
d) 就可以开始闲聊了。
注意：
1. 如果中文词典发生了变化，则需要重新编译生成中文同义词词库和对话样本库
2. 如果中文同义词库发生了变化，则需要重新编译生成对话样本库

当作库使用：
1. 可以将com.davidsoft中除掉simplechatsystem包后将剩余的内容打成Java包作为库使用
2. 实例化com.davidsoft.natural.chinese.ChattingSystem()类，一个此类的实例代表一个对话系统，维持着自己的聊天上下文
3. 循环调用此类的getAnswer()方法即可。
4. 具体使用方法参见源码相应注释。


