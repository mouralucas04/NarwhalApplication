package com.lucasmoura.narwhalapplication

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.markdown.markdown
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path

fun main(){
    val executor = simpleOpenAIExecutor(BuildConfig.API_KEY)

    val prompt = prompt("images-prompt"){
        system("You are a professional assistant that can write cool and funny descriptions for Instagram posts.")

        user {
            markdown {
                +"I want to create a new post on Instagram."
                br()
                +"Can you write brazilian portuguese something creative under my instagram post with the following photos?"
                br()
                h2("Requirements")
                bulleted {
                    item("It must be very funny and creative.")
                    item("It must increase my chance of becoming an ultra-famous blogger!!!!")
                    item("It not contain explicit content, harassment or bullying")
                    item("It must be a short catching phrase")
                    item("You must include relevant hashtags that would increase the visibility of my post")
                }
            }

            attachments {
                image(Path("C:\\Users\\ACER\\AndroidStudioProjects\\NarwhalApplication\\app\\src\\main\\java\\com\\lucasmoura\\narwhalapplication\\ui\\images\\kodee-loving.png"))
                image(Path("C:\\Users\\ACER\\AndroidStudioProjects\\NarwhalApplication\\app\\src\\main\\java\\com\\lucasmoura\\narwhalapplication\\ui\\images\\kodee-electrified.png"))
            }
        }
    }

    runBlocking {
        val response = executor.executeStreaming(prompt = prompt, model = OpenAIModels.Chat.GPT4o)
        response.collect { print(it) }
    }
}