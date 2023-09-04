package io.github.funnsam.wordle_bot;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.time.Instant;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

class WordleInstance {
	class Guess {
		public String guess;
		public GuessResult[] result;

		enum GuessResult {
			Correct, Exists, None
		}
		
		public Guess(String gu, GuessResult[] re) {
			guess = gu; result = re;
		}
	}

	public static List<String> answers;
	public static List<String> valid;

	static Random rand = new Random(Instant.now().getEpochSecond());
	
	String correct;
	List<Guess> guesses = new ArrayList<>();

	public static void init_words() throws Exception {
		answers = read_lines(WordleInstance.class.getResourceAsStream("/answers.txt"));
		valid = read_lines(WordleInstance.class.getResourceAsStream("/valid.txt"));
	}

	public static List<String> read_lines(InputStream in) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		List<String> out = new ArrayList<>();
		while (reader.ready()) {
			out.add(reader.readLine());
		}
		return out;
	}

	public WordleInstance() {
		correct = answers.get(rand.nextInt(answers.size()));
	}

	public void guess(String guess) {
		List<Guess.GuessResult> r = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			char c = guess.charAt(i);
			if (c == correct.charAt(i))  {
				r.add(Guess.GuessResult.Correct);
			} else if (correct.indexOf(c) != -1) {
				r.add(Guess.GuessResult.Exists);
			} else {
				r.add(Guess.GuessResult.None);
			}
		}

		guesses.add(new Guess(guess, r.toArray(new Guess.GuessResult[5])));
	}

	public MessageEmbed show() {
		String r = "";
		for (Guess g : guesses) {
			for (int i = 0; i < 5; i++) {
				switch (g.result[i]) {
					case Correct:
						r += ":green_circle:"; break;
					case Exists:
						r += ":yellow_circle:"; break;
					case None:
						r += ":black_circle:"; break;
				}
				r += "`" + Character.toUpperCase(g.guess.charAt(i)) + "`";
			}
			r += "\n";
		}

		if (is_solved()) {
			r += ":tada: Congratulations! You won!";
		} else if (is_game_over()) {
			r += String.format("<:despair:1139053884111851633> You lost! The word is [`%1$s`](https://google.com/search?q=%1$s+definition)!", correct);
		} else {
			r += String.format("Guess %d / 6", guesses.size());
		}

		EmbedBuilder eb = new EmbedBuilder()
			.setTitle("Wordle")
			.setDescription(r)
			.setColor(new Color(0x98c378));

		return eb.build();
	}

	public boolean is_solved() {
		Guess.GuessResult[] g = guesses.get(guesses.size()-1).result;
		for (Guess.GuessResult r : g) {
			if (r != Guess.GuessResult.Correct) return false;
		}
		
		return true;
	}

	public boolean is_game_over() {
		return (guesses.size() >= 6 && !is_solved());
	}

	public boolean should_remove() {
		return is_solved() || is_game_over();
	}
}
