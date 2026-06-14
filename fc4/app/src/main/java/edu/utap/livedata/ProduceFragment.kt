package edu.utap.livedata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import edu.utap.livedata.databinding.FragmentMainBinding

class ProduceFragment :  Fragment() {
    companion object {
        const val buttonText = "Produce"
    }
    // NB: This is the new way to initialize a viewModel that is shared
    // with the parent activity
    // XXX declare and initialize viewModel
    private val viewModel: ViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ProduceFragmentArgs by navArgs()
        binding.title.text = args.title
        binding.button.text = buttonText

        binding.button.setOnClickListener {
            viewModel.updateData((0..9999).random().toString())
        }
        // XXX Write me, observe live data
        viewModel.data.observe(viewLifecycleOwner,
            Observer { newValue ->
                binding.textView.text = newValue
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
