package edu.cs371m.triviagame.api

class Repository(private val api: TriviaApi) {
    // XXX Write me.
    suspend fun retrieveThree(level: String) : List<TriviaQuestion> {
        return api.getThree(level).results
    }
}