# Script for parsing such files:
#
# Package: Grid-Stepper-Game
# Version: 1.0-1
# Maintainer: Dmitriy Meleshko <dmytro.meleshko@gmail.com>
# Architecture: all
# Section: games
# Description: Game where you need to go from A to B stepping on all empty cells once
#  Nothing here.
# Depends: oracle-java8-installer
# Priority: optional
# Installed-Size: 189424
#
# As you can see, it's format of "control" file in .deb packages and looks like MANIFEST.MF from JARs.

import re


class IllegalFormatException(BaseException):
    pass


def parse_pairs(string):
    """
    Converts string where are data wrote using such method:

    Key: Value

    To dictionary where "Key" is key and "Value" is value. If there's newline, space and dot or text - that must be
    added to previous value.

    :param string: string that contains data to convert
    :return:
    :raises Exception
    """
    pairs = {}
    last_key = None

    for line in string.split('\n'):
        # If line is continuing of previous value - add it
        if re.match('( [^\n]+| \\.)', line) is not None:
            pairs[last_key] += '\n' + line

        else:
            # Regexp passes:
            # Key: Value
            # abc: DEF

            # Won't pass:
            # a adn dsj jsd dsi ads pf
            match = re.match('([^:]+): ([^\n]+)', line)

            if match is not None:
                pairs.update({match.group(1): match.group(2)})
                last_key = match.group(1)

            elif not re.match('\\s+|', line):
                raise IllegalFormatException("Line\n%s\nDoesn't match patterns "
                                             "\"([^:]+): ([^\\n]+) and \"( [^\\n]+| \\.)\"!" % line)

    return pairs
