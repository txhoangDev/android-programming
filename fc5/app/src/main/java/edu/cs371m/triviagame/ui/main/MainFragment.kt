package edu.cs371m.triviagame.ui.main

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import edu.cs371m.triviagame.MainViewModel
import edu.cs371m.triviagame.api.TriviaQuestion
import edu.cs371m.triviagame.databinding.MainFragmentBinding

class MainFragment : Fragment() {
    private var _binding : MainFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!
    // SafeArgs plugins
    private val args: MainFragmentArgs by navArgs()

    private val viewModel: MainViewModel by activityViewModels()
    // XXX initialize the viewModel

    private fun setClickListeners(question: TriviaQuestion, tv: TextView, tb: Button, fb: Button) {
        // XXX Write me Color.GREEN for correct, Color.RED for incorrect
        tb.setOnClickListener {
            if (question.correctAnswer == tb.text)
                tv.setBackgroundColor(Color.GREEN)
            else
                tv.setBackgroundColor(Color.RED)

            tb.isEnabled = false
            fb.isEnabled = false
        }

        fb.setOnClickListener {
            if (question.correctAnswer == fb.text)
                tv.setBackgroundColor(Color.GREEN)
            else
                tv.setBackgroundColor(Color.RED)

            tb.isEnabled = false
            fb.isEnabled = false
        }
    }
    // Corrects some ugly HTML encodings
    private fun fromHtml(source: String): String {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // XXX Write me.  viewModel should observe something
        // When it gets what it is observing, it should index into it
        // remember to call fromHtml on your string
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            resetQColor()
            binding.qTrueBut.isEnabled = true
            binding.qFalseBut.isEnabled = true
            val triviaQuestion = questions[args.questionNumber]
            binding.qTV.text = fromHtml(triviaQuestion.question)

            setClickListeners(triviaQuestion, binding.qTV, binding.qTrueBut, binding.qFalseBut)
        }
    }
    private fun resetQColor() {
        binding.qTV.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
