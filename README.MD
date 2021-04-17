# COSC 310 Individual Project -  Kenneth Oh

## APIs Added
Google Translate and Wikipedia API.

## Roles and Topics

<p>
This project is a chatbot that plays the role of a friend.
</p>
<p>
The conversation topics are same as the one from Team 2 Chatbot.
  
<p>Conversation topics: favorite food, hobbies, books, sports.</p>


### General

</li>

The bot is based on a question and answer format. It responds to questions with answers, and responds to answers with questions.

The bot has a knowledge base made of 3 components.
<ul>
<li>An arraylist of questions.</li>
<li>An arraylist of answers.</li>
<li>A hashmap of question-answerlist pairs. Each question in the hashmap has an arraylist of possible answers.</li>
</ul>
<br>
<p>
The knowlage base is stored in the knowledge.dat file.

If the input is "###" the bot will print its knowledge base.

If the input is "stop" the program will stop.
</p>

</ul>

<ul>
<li>

### Input Is An Answer
<ul>
<li>Bot adds answer to arraylist of answers bot checks previous response if the previous response was a question.</li>
<li>Bot adds answer to hashmap. bot attempts to ask relevant questions.</li>
<li>NEW - Bot translates answer in different language to English if a user answered in a language other than English. (Incomplete)</li>
</ul>
</li>


<li>

### Input Is A Question
<ul>
<li>bot adds a question to arraylist of questions</li>

<li>bot checks hashmap for question if question is in hashmap</li>

<li>respond with one of the mapped answers if question is not in hashmap</li>

<li>add question to hashmap attempt to respond with relevant answer</li>
  
<li>To come up with a relevant answer the bot parses the input string into words and then tries to respond with a string containing those words.</li>

<li>The bot will respond with a string that has the most matching words with the input string.</li>

<li>The bot also keeps track of the last 5 responses and does not reapeat them.</li>

<li>NEW - If a language other than English is detected, the bot will answer the question in English. (Incomplete)</li>

<li>NEW - The bot will translate a given text when asked by a user. For example, a user may ask: How do you write "Do you want to go out?" in Spanish? (Incomplete)</li>

<li>NEW - The bot will lookup Wikipedia website if the answer for the question can not be found in the knowledge base. For example, "What is photosynthesis?" (Incomplete)</li>

<li>NEW - Combination of Google Translate API and Wikipedia API. Users can ask the bot to translate a text found using Wikipedia API. (Incomplete)</li>
</ul>
</li>
</ul>


## Class Structure and Organization
A Chatbot has Knowledge, a language processor, and a GUI. Knowledge has a HashMap. A GUI has a Spellchecker. A language processor has a Pipeline. A text will be sent to Google Translate API or Wikipedia API as needed.

![Individual Chatbot DFD@2x](https://user-images.githubusercontent.com/77301335/115102976-fa737e00-9f13-11eb-9acb-df2d78bfb9c3.png)


## Conversion Topics

The bot is capable of talking about anything in its knowledge base. To expand the knowledge base the user simply has to 

start talking to the bot about it. Users can now ask / answer questions in a language other than English.


## Limitations

The API (Google Translate and Wikipedia) could not be implemented in Chatbot.java. The documentations above explains how the chatbot should work when it is fully implemented and functional.



