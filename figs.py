from pathlib import Path
from subprocess import run, PIPE

import seaborn as sns
import pandas as pd

JAVA_HOME = Path.home() / 'graal/loom/build/macosx-x86_64-server-release/images/graal-builder-jdk'


def verify_get_loom(java='java'):
    assert JAVA_HOME.exists()

    return JAVA_HOME / f'bin/{java}'


def run_plot(start, end, step, title, out):
    r = run([verify_get_loom('java'), 'bench/loom/Main'] + [str(i) for i in (start, end, step)],
            cwd='src', stdout=PIPE)
    assert r.returncode == 0
    times = [
        [int(line.split(' ')[0]), int(line.split(' ')[i]), ['vthread', 'thread'][i - 1]]
        for i in (1, 2)
        for line in r.stdout.decode().strip().split('\n')
    ]
    data = pd.DataFrame(times, columns=['iteration', 'time', 'type'])

    fig = sns.lmplot(x="iteration", y="time", hue="type", data=data)
    fig.set(title=title)
    fig.savefig(out)


if __name__ == '__main__':
    assert run([verify_get_loom('javac'), 'bench/loom/Main.java'], cwd='src').returncode == 0
    run_plot(100, 10000, 100, 'thread vs vthread on simple nop loop', 'full.png')
