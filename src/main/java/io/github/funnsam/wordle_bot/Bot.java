package io.github.funnsam.wordle_bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.util.HashMap;

public class Bot extends ListenerAdapter {
	static JDA jda;
	static HashMap<Long, WordleInstance> wordle = new HashMap<>();

    public static void main(String[] args) throws Exception {
		WordleInstance.init_words();

		String api_key = args[0];

		jda = JDABuilder.createDefault(api_key)
			.addEventListeners(new Bot())
			.build();

		jda.awaitReady();
    }

	@Override
	public void onReady(ReadyEvent event) {
		jda.updateCommands().addCommands(
			Commands.slash("start", "Start a Wordle game"),
			Commands.slash("show", "Show your current Wordle game"),
			Commands.slash("cheat", "Hehe"),
			Commands.slash("guess", "Play Wordle")
				.addOption(OptionType.STRING, "guess", "The word you guess", true)
		).queue();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getName()) {
			case "start": {
				wordle.put(event.getMember().getIdLong(), new WordleInstance());
				event.reply("Done!").queue();
				break;
			}
			case "show": {
				WordleInstance w = wordle.get(event.getMember().getIdLong());
				if (w != null) {
					event.replyEmbeds(w.show()).queue();
				} else {
					event.reply("Please run `/start` first!").queue();
				}
				break;
			}
			case "cheat": {
				WordleInstance w = wordle.get(event.getMember().getIdLong());
				if (w != null) {
					event.reply(w.correct).queue();
				} else {
					event.reply("Please run `/start` first!").queue();
				}
				break;
			}
			case "guess": {
				WordleInstance w = wordle.get(event.getMember().getIdLong());
				if (w != null) {
					String guess = event.getOption("guess").getAsString();
					if (guess.length() != 5) {
						event.reply("Please submit a guess with 5 characters!").queue();
					} else if (!WordleInstance.valid.contains(guess)) {
						event.reply("Please submit a correct word!").queue();
					} else {
						w.guess(guess.toLowerCase());
						event.replyEmbeds(w.show()).queue();
						if (w.should_remove()) {
							wordle.remove(event.getMember().getIdLong());
						}
					}
				} else {
					event.reply("Please run `/start` first!").queue();
				}
				break;
			}
		}
	}
}
