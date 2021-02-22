import os
from pathlib import Path
from subprocess import run, PIPE

import seaborn as sns
import pandas as pd

JAVA_HOME = Path.home() / 'graal/loom/build/linux-x86_64-server-release/images/graal-builder-jdk'
SVM_HOME = Path.home() / 'graal/graal-enterprise/substratevm-enterprise'


def verify_get_loom(java='java'):
    assert JAVA_HOME.exists()

    return JAVA_HOME / f'bin/{java}'


def verify_get_mx():
    assert SVM_HOME.exists()

    return SVM_HOME.parents[1] / 'mx/mx'


def run_loom_plot(start, end, step, title, out):
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
    run_loom_plot(100, 10000, 100, 'thread vs vthread on simple nop loop', 'loom.png')

    assert run([verify_get_loom('javac'), 'bench/Main.java'], cwd='src').returncode == 0
    assert run(['cp', '-r', 'bench', str(SVM_HOME)], cwd='src').returncode == 0
    assert run(
        [verify_get_mx(), 'native-image',
         '-H:Optimize=0', '-H:+UseLoom',
         'bench.Main'], cwd=SVM_HOME,
        env={'JAVA_HOME': str(JAVA_HOME.resolve()), 'PATH': os.getenv('PATH')}
    ).returncode == 0

    # cont
    results = []
    for i in range(0, 200000, 10000):
        r = run([verify_get_loom('java'), 'bench/Main', 'loom', 'cont', f'{i}'],
                cwd='src', stdout=PIPE)
        loom_results = r.stdout.decode().strip().split('\n')

        r = run(['./bench.main', 'loom', 'cont', f'{i}'],
                cwd=SVM_HOME, stdout=PIPE)
        svm_results = r.stdout.decode().strip().split('\n')

        results.append(['loom', i, int(loom_results[1])])
        results.append(['svm', i, int(svm_results[1])])

    fig = sns.lmplot(x="iteration", y="time", hue="type",
                     data=pd.DataFrame(results, columns=['type', 'iteration', 'time']))
    fig.set(title='nop continuation')
    fig.savefig('cont.png')

    # vthread
    results = []
    for i in range(0, 200000, 10000):
        r = run([verify_get_loom('java'), 'bench/Main', 'loom', 'vthread', f'{i}'],
                cwd='src', stdout=PIPE)
        loom_results = r.stdout.decode().strip().split('\n')

        r = run(['./bench.main', 'loom', 'vthread', f'{i}'],
                cwd=SVM_HOME, stdout=PIPE)
        svm_results = r.stdout.decode().strip().split('\n')

        results.append(['loom', i, int(loom_results[1])])
        results.append(['svm', i, int(svm_results[1])])

    fig = sns.lmplot(x="iteration", y="time", hue="type",
                     data=pd.DataFrame(results, columns=['type', 'iteration', 'time']))
    fig.set(title='nop vthread')
    fig.savefig('vthread.png')

    # yield cont
    results = []
    for i in range(1, 15):
        r = run([verify_get_loom('java'), 'bench/Main', 'loom', 'yields-cont', '10000', f'{i}'],
                cwd='src', stdout=PIPE)
        loom_results = r.stdout.decode().strip().split('\n')

        r = run(['./bench.main', 'svm', 'yields-cont', '10000', f'{i}'],
                cwd=SVM_HOME, stdout=PIPE)
        svm_results = r.stdout.decode().strip().split('\n')

        results.append(['loom', i, int(loom_results[1])])
        results.append(['svm', i, int(svm_results[1])])

    data = pd.DataFrame(results, columns=['type', 'depth', 'time'])
    loom_data = data[data['type'] == 'loom']

    fig = sns.lmplot(x="depth", y="time", hue="type", data=data)
    fig.set(title='yield at every level')
    fig.savefig('yield-cont.png')
    fig = sns.lmplot(x="depth", y="time", hue="type", data=loom_data)
    fig.set(title='yield at every level')
    fig.savefig('yield-cont-loom.png')

    # yield vthread
    results = []
    for i in range(1, 15):
        r = run([verify_get_loom('java'), 'bench/Main', 'loom', 'yields-cont', '10000', f'{i}'],
                cwd='src', stdout=PIPE)
        loom_results = r.stdout.decode().strip().split('\n')

        r = run(['./bench.main', 'svm', 'yields-cont', '10000', f'{i}'],
                cwd=SVM_HOME, stdout=PIPE)
        svm_results = r.stdout.decode().strip().split('\n')

        results.append(['loom', i, int(loom_results[1])])
        results.append(['svm', i, int(svm_results[1])])

    data = pd.DataFrame(results, columns=['type', 'depth', 'time'])
    loom_data = data[data['type'] == 'loom']

    fig = sns.lmplot(x="depth", y="time", hue="type", data=data)
    fig.set(title='yield at every level')
    fig.savefig('yield-vthread.png')
    fig = sns.lmplot(x="depth", y="time", hue="type", data=loom_data)
    fig.set(title='yield at every level')
    fig.savefig('yield-vthread-loom.png')
