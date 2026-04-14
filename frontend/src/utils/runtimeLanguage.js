export const runtimeLanguageCatalog = [
  {
    code: 'java',
    label: 'Java',
    monacoLanguage: 'java',
    defaultFileName: 'Main.java',
    defaultStarterCode: `public class Main {
    public static void main(String[] args) throws Exception {

    }
}
`
  },
  {
    code: 'python',
    label: 'Python',
    monacoLanguage: 'python',
    defaultFileName: 'main.py',
    defaultStarterCode: `import sys


def main():
    pass


if __name__ == "__main__":
    main()
`
  },
  {
    code: 'cpp',
    label: 'C++',
    monacoLanguage: 'cpp',
    defaultFileName: 'main.cpp',
    defaultStarterCode: `#include <iostream>
using namespace std;

int main() {
    return 0;
}
`
  },
  {
    code: 'javascript',
    label: 'JavaScript',
    monacoLanguage: 'javascript',
    defaultFileName: 'main.js',
    defaultStarterCode: `function main() {
}

main();
`
  },
  {
    code: 'typescript',
    label: 'TypeScript',
    monacoLanguage: 'typescript',
    defaultFileName: 'main.ts',
    defaultStarterCode: `function main(): void {
}

main();
`
  },
  {
    code: 'go',
    label: 'Go',
    monacoLanguage: 'go',
    defaultFileName: 'main.go',
    defaultStarterCode: `package main

func main() {
}
`
  }
]

export const normalizeRuntimeLanguage = (language) => String(language || '').trim().toLowerCase()

export const getRuntimeLanguageDefinition = (language) => {
  const code = normalizeRuntimeLanguage(language)
  return runtimeLanguageCatalog.find((item) => item.code === code) || null
}

export const getRuntimeLanguageLabel = (language) => {
  const definition = getRuntimeLanguageDefinition(language)
  return definition?.label || (language ? String(language).toUpperCase() : '--')
}

export const getRuntimeMonacoLanguage = (language) => {
  const definition = getRuntimeLanguageDefinition(language)
  return definition?.monacoLanguage || 'text'
}

export const getRuntimeDefaultFileName = (language) => {
  const definition = getRuntimeLanguageDefinition(language)
  return definition?.defaultFileName || 'main.txt'
}

export const getRuntimeDefaultStarterCode = (language) => {
  const definition = getRuntimeLanguageDefinition(language)
  return definition?.defaultStarterCode || ''
}
